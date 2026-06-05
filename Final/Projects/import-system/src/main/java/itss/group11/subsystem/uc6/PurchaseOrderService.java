package itss.group11.subsystem.uc6;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itss.group11.entity.chung.OrderRequest;
import itss.group11.entity.chung.PurchaseOrder;
import itss.group11.entity.chung.PurchaseOrderLine;
import itss.group11.entity.uc6.PartialOrderSelectionDTO;
import itss.group11.entity.uc6.PurchaseOrderResponseDTO;
import itss.group11.entity.uc6.ReceivedLineDTO;
import itss.group11.entity.uc6.ReconciliationDetailDTO;
import itss.group11.entity.uc6.ReconciliationResultDTO;
import itss.group11.entity.uc6.ReconciliationSubmitDTO;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.chung.PurchaseOrderLineRepository;
import itss.group11.subsystem.chung.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final OrderRequestRepository orderRequestRepository;
    private final ReconciliationValidator reconciliationValidator;
    private final WarehouseInventoryService warehouseInventoryService;
    private final DiscrepancyReportService discrepancyReportService;

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponseDTO> getInTransitOrders() {
        return purchaseOrderRepository
                .findByStatusOrderByCreatedAtDesc(PurchaseOrder.PurchaseOrderStatus.IN_TRANSIT)
                .stream()
                .map(this::toPurchaseOrderResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReconciliationDetailDTO getReconciliationDetail(String orderId) {
        PurchaseOrder purchaseOrder = findInTransitOrder(orderId);
        List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderOrderByIdAsc(purchaseOrder);

        return ReconciliationDetailDTO.builder()
                .orderId(purchaseOrder.getOrderId())
                .requestCode(purchaseOrder.getOrderRequest() == null ? "" : purchaseOrder.getOrderRequest().getRequestCode())
                .siteCode(purchaseOrder.getSite() == null ? "" : purchaseOrder.getSite().getSiteCode())
                .siteName(purchaseOrder.getSite() == null ? "" : purchaseOrder.getSite().getSiteName())
                .status(purchaseOrder.getStatus().name())
                .lines(lines.stream().map(this::toLineDTO).toList())
                .build();
    }

    @Transactional
    public ReconciliationResultDTO reconcile(String orderId, ReconciliationSubmitDTO dto) {
        Map<Long, ReceivedLineDTO> receivedByLineId = reconciliationValidator.toReceivedLineMap(dto);
        PurchaseOrder purchaseOrder = findInTransitOrder(orderId);
        List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderOrderByIdAsc(purchaseOrder);
        reconciliationValidator.requireOnlyExistingLines(receivedByLineId, lines);

        boolean hasDiscrepancy = false;
        List<String> reportIds = new ArrayList<>();

        for (PurchaseOrderLine line : lines) {
            ReceivedLineDTO receivedLine = reconciliationValidator.requireReceivedLine(receivedByLineId, line);
            int receivedQty = receivedLine.getReceivedQty();

            line.setReceivedQty(receivedQty);
            purchaseOrderLineRepository.save(line);
            warehouseInventoryService.increaseInternalInventory(line.getMerchandise(), receivedQty);

            discrepancyReportService
                    .createIfDiscrepant(purchaseOrder, line, receivedQty, dto)
                    .ifPresent(reportId -> {
                        reportIds.add(reportId);
                    });
            hasDiscrepancy = !reportIds.isEmpty();
        }

        purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.RECEIVED);
        purchaseOrderRepository.save(purchaseOrder);
        completeOrderRequestIfApplicable(purchaseOrder);

        return ReconciliationResultDTO.builder()
                .orderId(purchaseOrder.getOrderId())
                .status(purchaseOrder.getStatus().name())
                .hasDiscrepancy(hasDiscrepancy)
                .message(hasDiscrepancy
                        ? "Da xac nhan nhap kho, cap nhat ton kho noi bo va lap bien ban sai lech."
                        : "Da xac nhan nhap kho va cap nhat ton kho noi bo, khong co sai lech.")
                .discrepancyReportIds(reportIds)
                .build();
    }

    private void completeOrderRequestIfApplicable(PurchaseOrder reconciledOrder) {
        OrderRequest linkedRequest = reconciledOrder.getOrderRequest();
        if (linkedRequest == null || linkedRequest.getRequestCode() == null || linkedRequest.getRequestCode().isBlank()) {
            return;
        }

        String requestCode = linkedRequest.getRequestCode();
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findByOrderRequest_RequestCode(requestCode);
        if (purchaseOrders.isEmpty()) {
            return;
        }

        boolean allFinished = purchaseOrders.stream()
                .allMatch(order -> order.getStatus() == PurchaseOrder.PurchaseOrderStatus.RECEIVED
                        || order.getStatus() == PurchaseOrder.PurchaseOrderStatus.CANCELLED);
        if (!allFinished) {
            return;
        }

        boolean hasReceivedOrder = purchaseOrders.stream()
                .anyMatch(order -> order.getStatus() == PurchaseOrder.PurchaseOrderStatus.RECEIVED);
        if (!hasReceivedOrder) {
            return;
        }

        orderRequestRepository.findByRequestCode(requestCode).ifPresent(orderRequest -> {
            if (orderRequest.getStatus() == OrderRequest.OrderRequestStatus.ORDERED) {
                orderRequest.setStatus(OrderRequest.OrderRequestStatus.COMPLETED);
                orderRequestRepository.save(orderRequest);
            }
        });
    }

    private PurchaseOrder findInTransitOrder(String orderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay don dat hang: " + orderId));

        reconciliationValidator.requireInTransit(purchaseOrder);
        return purchaseOrder;
    }

    private PurchaseOrderResponseDTO toPurchaseOrderResponseDTO(PurchaseOrder purchaseOrder) {
        return PurchaseOrderResponseDTO.builder()
                .orderId(purchaseOrder.getOrderId())
                .requestCode(purchaseOrder.getOrderRequest() == null ? "" : purchaseOrder.getOrderRequest().getRequestCode())
                .siteCode(purchaseOrder.getSite() == null ? "" : purchaseOrder.getSite().getSiteCode())
                .siteName(purchaseOrder.getSite() == null ? "" : purchaseOrder.getSite().getSiteName())
                .status(purchaseOrder.getStatus().name())
                .createdDate(purchaseOrder.getCreatedAt() == null ? "" : purchaseOrder.getCreatedAt().toLocalDate().toString())
                .build();
    }

    private PartialOrderSelectionDTO toLineDTO(PurchaseOrderLine line) {
        int orderedQty = line.getOrderedQty() == null ? 0 : line.getOrderedQty();
        int receivedQty = line.getReceivedQty() == null ? orderedQty : line.getReceivedQty();
        return PartialOrderSelectionDTO.builder()
                .lineId(line.getId())
                .merchandiseCode(line.getMerchandise() == null ? "" : line.getMerchandise().getCode())
                .merchandiseName(line.getMerchandise() == null ? "" : line.getMerchandise().getName())
                .orderedQty(orderedQty)
                .receivedQty(receivedQty)
                .differenceQty(orderedQty - receivedQty)
                .unitPrice(line.getMerchandise() == null ? null : line.getMerchandise().getUnitPrice())
                .unit(line.getUnit())
                .build();
    }
}
