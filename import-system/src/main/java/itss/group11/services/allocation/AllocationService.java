package itss.group11.services.allocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itss.group11.dto.allocation.AllocationResultDTO;
import itss.group11.models.ImportSite;
import itss.group11.models.OrderRequest;
import itss.group11.models.OrderRequestItem;
import itss.group11.models.PurchaseOrder;
import itss.group11.models.PurchaseOrderLine;
import itss.group11.repository.allocation.ImportSiteRepository;
import itss.group11.repository.orderExecution.PurchaseOrderRepository;
import itss.group11.repository.requestManage.OrderRequestRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final OrderRequestRepository orderRequestRepository;
    private final ImportSiteRepository importSiteRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryCheckService inventoryCheckService; // Thêm Service kiểm kho thực tế

    /**
     * Hàm chính xử lý lập kế hoạch đặt hàng (UC005)
     */
    @Transactional
    public AllocationResultDTO processAllocationPlan(String requestCode) {
        // 1. Lấy thông tin yêu cầu nhập hàng từ tầng bán hàng
        OrderRequest request = orderRequestRepository.findByRequestCode(requestCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhập hàng: " + requestCode));

        if (request.getStatus() != OrderRequest.OrderRequestStatus.PENDING) {
            throw new RuntimeException("Yêu cầu này đã được xử lý từ trước!");
        }

        // 2. Kịch bản kiểm tra tồn kho THẬT qua kết nối Supabase Postgres
        boolean isStockEnough = checkInventoryFromSites(request);

        if (!isStockEnough) {
            throw new RuntimeException("INSUFFICIENT_INVENTORY"); // Trả lỗi nếu không đủ hàng
        }

        // 3. Thực thi thuật toán phân bổ và tạo PO (Order Execution)
        List<String> poCodes = executeOptimalAllocationAndCreatePO(request);

        // 4. Cập nhật trạng thái yêu cầu thành hoàn tất
        request.setStatus(OrderRequest.OrderRequestStatus.ORDERED);
        orderRequestRepository.save(request);

        return AllocationResultDTO.builder()
                .requestCode(requestCode)
                .isSuccess(true)
                .message("Phân bổ kế hoạch đặt hàng thành công!")
                .generatedPoCodes(poCodes)
                .build();
    }

    /**
     * Kiểm tra tồn kho THẬT từ Database cho toàn bộ danh sách mặt hàng trong đơn
     */
    private boolean checkInventoryFromSites(OrderRequest request) {
        for (OrderRequestItem reqItem : request.getItems()) {
            String merchandiseCode = reqItem.getMerchandise().getCode();
            int requiredQty = reqItem.getQuantityOrdered();

            // Gọi sang InventoryCheckService để quét DB
            if (!inventoryCheckService.isStockSufficient(merchandiseCode, requiredQty)) {
                return false; // Chỉ cần 1 mặt hàng thiếu là luồng lập kế hoạch bị ngắt để xử lý thủ công
            }
        }
        return true; 
    }

    /**
     * Thuật toán phân bổ và sinh đơn Purchase Order
     */
    private List<String> executeOptimalAllocationAndCreatePO(OrderRequest request) {
        List<String> generatedPoCodes = new ArrayList<>();

        ImportSite optimalSite = importSiteRepository.findBySiteCode("SITE_JP_01")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Site cấu hình mặc định"));

        // Tạo Đơn đặt hàng (PO) mới tương thích với PurchaseOrder Model
        PurchaseOrder newPo = PurchaseOrder.builder()
                .orderId(generateUniquePoCode())                
                .orderRequest(request)                          
                .site(optimalSite)                              
                .status(PurchaseOrder.PurchaseOrderStatus.CREATED) 
                .orderLines(new ArrayList<>())                  
                .build();

        // Duyệt qua các mặt hàng và tạo PurchaseOrderLine
        for (OrderRequestItem reqItem : request.getItems()) {
            PurchaseOrderLine poItem = PurchaseOrderLine.builder()
                    .purchaseOrder(newPo)
                    .merchandise(reqItem.getMerchandise())
                    .orderedQty(reqItem.getQuantityOrdered())   
                    .build();
            
            newPo.getOrderLines().add(poItem);                  
        }

        // Lưu xuống Database
        purchaseOrderRepository.save(newPo);
        generatedPoCodes.add(newPo.getOrderId());               

        return generatedPoCodes;
    }

    private String generateUniquePoCode() {
        return "DH-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}