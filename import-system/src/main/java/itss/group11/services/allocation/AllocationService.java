package itss.group11.services.allocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itss.group11.dto.allocation.AllocationPlanDTO;
import itss.group11.dto.allocation.AllocationPlanItemDTO;
import itss.group11.dto.allocation.AllocationRequestRowDTO;
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

    @Transactional(readOnly = true)
    public List<AllocationRequestRowDTO> getPendingRequests() {
        return orderRequestRepository
                .findByStatusOrderByCreatedAtAsc(OrderRequest.OrderRequestStatus.PENDING)
                .stream()
                .map(request -> AllocationRequestRowDTO.builder()
                        .requestCode(request.getRequestCode())
                        .status(request.getStatus().name())
                        .createdDate(request.getCreatedAt() == null ? "" : request.getCreatedAt().toLocalDate().toString())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public AllocationPlanDTO previewAllocationPlan(String requestCode) {
        OrderRequest request = findPendingRequest(requestCode);

        List<AllocationPlanItemDTO> planItems = new ArrayList<>();
        boolean isStockEnough = true;

        for (OrderRequestItem reqItem : request.getItems()) {
            Merchandise merchandise = reqItem.getMerchandise();
            int remainingQty = reqItem.getQuantityOrdered();

            List<SiteStockDTO> availableStocks =
                    inventoryCheckService.getStockDetailsAcrossSites(merchandise.getCode());

            for (SiteStockDTO stock : availableStocks) {
                if (remainingQty <= 0) break;

                int qtyToAllocate = Math.min(remainingQty, stock.getInStockQuantity());
                remainingQty -= qtyToAllocate;

                planItems.add(AllocationPlanItemDTO.builder()
                        .merchandiseCode(merchandise.getCode())
                        .siteCode(stock.getSiteCode())
                        .siteName(stock.getSiteName())
                        .allocatedQuantity(qtyToAllocate)
                        .deliveryMeans(suggestDeliveryMeans(stock).name())
                        .build());
            }

            if (remainingQty > 0) {
                isStockEnough = false;
            }
        }

        return AllocationPlanDTO.builder()
                .requestCode(requestCode)
                .isEnoughInventory(isStockEnough)
                .planItems(planItems)
                .message(isStockEnough ? "Kế hoạch khả thi." : "Cảnh báo: Không đủ tồn kho!")
                .build();
    }

    @Transactional
    public AllocationResultDTO processAllocationPlan(String requestCode) {
        OrderRequest request = findPendingRequest(requestCode);

        if (!checkInventoryFromSites(request)) {
            throw new RuntimeException("INSUFFICIENT_INVENTORY");
        }

        List<String> poCodes = executeOptimalAllocationAndCreatePO(request);

        request.setStatus(OrderRequest.OrderRequestStatus.ORDERED);
        orderRequestRepository.save(request);

        return AllocationResultDTO.builder()
                .requestCode(requestCode)
                .isSuccess(true)
                .message("Phân bổ thành công! Đã tạo " + poCodes.size() + " đơn PO.")
                .generatedPoCodes(poCodes)
                .build();
    }

    private OrderRequest findPendingRequest(String requestCode) {
        OrderRequest request = orderRequestRepository.findByRequestCode(requestCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhập hàng: " + requestCode));

        if (request.getStatus() != OrderRequest.OrderRequestStatus.PENDING) {
            throw new RuntimeException("Yêu cầu này không ở trạng thái PENDING.");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Yêu cầu nhập hàng chưa có mặt hàng.");
        }

        return request;
    }

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

    private List<String> executeOptimalAllocationAndCreatePO(OrderRequest request) {
        List<String> generatedPoCodes = new ArrayList<>();
        Map<String, PurchaseOrder> poGroupedBySite = new HashMap<>();

        for (OrderRequestItem reqItem : request.getItems()) {
            Merchandise merchandise = reqItem.getMerchandise();
            int remainingQtyToFulfill = reqItem.getQuantityOrdered();

            List<SiteStockDTO> availableStocks =
                    inventoryCheckService.getStockDetailsAcrossSites(merchandise.getCode());

            for (SiteStockDTO stock : availableStocks) {
                if (remainingQtyToFulfill <= 0) break;

                int qtyToAllocate = Math.min(remainingQtyToFulfill, stock.getInStockQuantity());
                remainingQtyToFulfill -= qtyToAllocate;

                String siteCode = stock.getSiteCode();

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

                PurchaseOrderLine poLine = PurchaseOrderLine.builder()
                        .purchaseOrder(sitePo)
                        .merchandise(merchandise)
                        .orderedQty(qtyToAllocate)
                        .receivedQty(0)
                        .unit(merchandise.getUnit())
                        .deliveryMeans(suggestDeliveryMeans(stock))
                        .build();

                sitePo.getOrderLines().add(poLine);
            }

            if (remainingQtyToFulfill > 0) {
                throw new RuntimeException("Không đủ hàng cho mặt hàng: " + merchandise.getCode());
            }
        }

        for (PurchaseOrder completedPo : poGroupedBySite.values()) {
            purchaseOrderRepository.save(completedPo);
            generatedPoCodes.add(completedPo.getOrderId());
        }

        return generatedPoCodes;
    }

    private String generateUniquePoCode() {
        String poCode;
        do {
            poCode = "DH-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (purchaseOrderRepository.existsByOrderId(poCode));
        return poCode;
    }

    private PurchaseOrderLine.DeliveryMeans suggestDeliveryMeans(SiteStockDTO stock) {
        Integer daysByShip = stock.getDaysByShip();
        Integer daysByAir = stock.getDaysByAir();

        if (daysByShip != null) {
            return PurchaseOrderLine.DeliveryMeans.SHIP;
        }

        if (daysByAir != null) {
            return PurchaseOrderLine.DeliveryMeans.AIR;
        }

        throw new RuntimeException("Site " + stock.getSiteCode()
                + " chua co du lieu ngay van chuyen bang AIR/SHIP.");
    }
}
