package itss.group11.services.allocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itss.group11.dto.allocation.AllocationResultDTO;
import itss.group11.dto.allocation.SiteStockDTO;
import itss.group11.models.ImportSite;
import itss.group11.models.Merchandise;
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
    private final InventoryCheckService inventoryCheckService; 

    /**
     * Hàm chính xử lý lập kế hoạch đặt hàng (UC005)
     */
    @Transactional
    public AllocationResultDTO processAllocationPlan(String requestCode) {
        // 1. Lấy thông tin yêu cầu
        OrderRequest request = orderRequestRepository.findByRequestCode(requestCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhập hàng: " + requestCode));

        if (request.getStatus() != OrderRequest.OrderRequestStatus.PENDING) {
            throw new RuntimeException("Yêu cầu này đã được xử lý!");
        }

        // 2. Kịch bản kiểm tra tồn kho tổng
        boolean isStockEnough = checkInventoryFromSites(request);

        if (!isStockEnough) {
            throw new RuntimeException("INSUFFICIENT_INVENTORY");
        }

        // 3. Thực thi thuật toán phân bổ đa Site và tạo các PO
        List<String> poCodes = executeOptimalAllocationAndCreatePO(request);

        // 4. Cập nhật trạng thái
        request.setStatus(OrderRequest.OrderRequestStatus.ORDERED);
        orderRequestRepository.save(request);

        return AllocationResultDTO.builder()
                .requestCode(requestCode)
                .isSuccess(true)
                .message("Phân bổ thành công! Đã lên kế hoạch mua hàng.")
                .generatedPoCodes(poCodes)
                .build();
    }

    /**
     * Kiểm tra tồn kho THẬT từ Database
     */
    private boolean checkInventoryFromSites(OrderRequest request) {
        for (OrderRequestItem reqItem : request.getItems()) {
            String merchandiseCode = reqItem.getMerchandise().getCode();
            int requiredQty = reqItem.getQuantityOrdered();

            if (!inventoryCheckService.isStockSufficient(merchandiseCode, requiredQty)) {
                return false; 
            }
        }
        return true; 
    }

    /**
     * Thuật toán phân bổ và sinh đơn Purchase Order theo từng Site
     */
    private List<String> executeOptimalAllocationAndCreatePO(OrderRequest request) {
        List<String> generatedPoCodes = new ArrayList<>();
        
        // Dùng Map để gom nhóm các PO theo từng Site (Key: siteCode, Value: PurchaseOrder)
        Map<String, PurchaseOrder> poGroupedBySite = new HashMap<>();

        // Duyệt qua từng mặt hàng cần đặt
        for (OrderRequestItem reqItem : request.getItems()) {
            Merchandise merchandise = reqItem.getMerchandise();
            int remainingQtyToFulfill = reqItem.getQuantityOrdered();

            // Lấy danh sách tồn kho của mặt hàng này (đã sort giảm dần số lượng từ DB)
            List<SiteStockDTO> availableStocks = inventoryCheckService.getStockDetailsAcrossSites(merchandise.getCode());

            for (SiteStockDTO stock : availableStocks) {
                if (remainingQtyToFulfill <= 0) break; // Đã gom đủ số lượng cho mặt hàng này

                // Tính toán số lượng có thể lấy từ Site hiện tại
                int qtyToAllocate = Math.min(remainingQtyToFulfill, stock.getInStockQuantity());
                remainingQtyToFulfill -= qtyToAllocate; // Trừ đi phần vừa phân bổ

                String siteCode = stock.getSiteCode();
                
                // Lấy PO của Site này ra (nếu chưa có thì tạo mới và ném vào Map)
                PurchaseOrder sitePo = poGroupedBySite.computeIfAbsent(siteCode, key -> {
                    ImportSite site = importSiteRepository.findBySiteCode(key)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy Site cấu hình: " + key));
                    
                    return PurchaseOrder.builder()
                            .orderId(generateUniquePoCode())
                            .orderRequest(request)
                            .site(site)
                            .status(PurchaseOrder.PurchaseOrderStatus.CREATED)
                            .orderLines(new ArrayList<>())
                            .build();
                });

                // Tạo line item mới và add vào PO của Site tương ứng
                PurchaseOrderLine poLine = PurchaseOrderLine.builder()
                        .purchaseOrder(sitePo)
                        .merchandise(merchandise)
                        .orderedQty(qtyToAllocate)
                        .build();
                
                sitePo.getOrderLines().add(poLine);
            }

            // Chốt chặn an toàn (bước checkInventory đã cover, nhưng vẫn nên có)
            if (remainingQtyToFulfill > 0) {
                throw new RuntimeException("Lỗi Logic hệ thống: Không thể phân bổ đủ hàng cho " + merchandise.getCode());
            }
        }

        // Lưu toàn bộ các đơn Purchase Order đã phân bổ xuống Database
        for (PurchaseOrder completedPo : poGroupedBySite.values()) {
            purchaseOrderRepository.save(completedPo);
            generatedPoCodes.add(completedPo.getOrderId());
        }

        return generatedPoCodes;
    }

    private String generateUniquePoCode() {
        return "DH-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}