package itss.group11.services.orderExecution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itss.group11.dto.orderExecution.PartialOrderSelectionDTO;
import itss.group11.dto.orderExecution.PurchaseOrderResponseDTO;
import itss.group11.dto.orderExecution.ReceivedLineDTO;
import itss.group11.dto.orderExecution.ReconciliationDetailDTO;
import itss.group11.dto.orderExecution.ReconciliationResultDTO;
import itss.group11.dto.orderExecution.ReconciliationSubmitDTO;
import itss.group11.models.DiscrepancyReport;
import itss.group11.models.InternalWarehouseInventory;
import itss.group11.models.Merchandise;
import itss.group11.models.PurchaseOrder;
import itss.group11.models.PurchaseOrderLine;
import itss.group11.repository.orderExecution.PurchaseOrderLineRepository;
import itss.group11.repository.orderExecution.PurchaseOrderRepository;
import itss.group11.repository.warehouse.DiscrepancyReportRepository;
import itss.group11.repository.warehouse.InternalWarehouseInventoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final DiscrepancyReportRepository discrepancyReportRepository;
    private final InternalWarehouseInventoryRepository internalWarehouseInventoryRepository;

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
        validateSubmitDTO(dto);

        PurchaseOrder purchaseOrder = findInTransitOrder(orderId);
        List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderOrderByIdAsc(purchaseOrder);
        Map<Long, ReceivedLineDTO> receivedByLineId = dto.getLines()
                .stream()
                .collect(Collectors.toMap(ReceivedLineDTO::getLineId, Function.identity()));

        List<String> reportIds = new ArrayList<>();
        boolean hasDiscrepancy = false;

        for (PurchaseOrderLine line : lines) {
            ReceivedLineDTO receivedLine = receivedByLineId.get(line.getId());
            if (receivedLine == null) {
                throw new RuntimeException("Chua nhap so luong thuc nhan cho dong hang ID: " + line.getId());
            }

            Integer receivedQty = receivedLine.getReceivedQty();
            if (receivedQty == null || receivedQty < 0) {
                throw new RuntimeException("So luong thuc nhan khong hop le cho dong hang ID: " + line.getId());
            }

            line.setReceivedQty(receivedQty);
            purchaseOrderLineRepository.save(line);
            increaseInternalInventory(line.getMerchandise(), receivedQty);

            int differenceQty = line.getOrderedQty() - receivedQty;
            if (differenceQty != 0) {
                hasDiscrepancy = true;
                DiscrepancyReport report = DiscrepancyReport.builder()
                        .reportId(generateReportId())
                        .purchaseOrder(purchaseOrder)
                        .merchandise(line.getMerchandise())
                        .orderedQty(line.getOrderedQty())
                        .receivedQty(receivedQty)
                        .differenceQty(differenceQty)
                        .reason(normalizeText(dto.getReason(), "Sai lech so luong khi doi soat nhap kho"))
                        .note(normalizeText(dto.getNote(), ""))
                        .createdBy(normalizeText(dto.getCreatedBy(), "Nhan vien kho"))
                        .build();

                reportIds.add(discrepancyReportRepository.save(report).getReportId());
            }
        }

        purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.RECEIVED);
        purchaseOrderRepository.save(purchaseOrder);

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

    private void increaseInternalInventory(Merchandise merchandise, int receivedQty) {
        if (merchandise == null || receivedQty <= 0) {
            return;
        }

        InternalWarehouseInventory inventory = internalWarehouseInventoryRepository
                .findByMerchandise_Code(merchandise.getCode())
                .orElseGet(() -> InternalWarehouseInventory.builder()
                        .merchandise(merchandise)
                        .inStockQuantity(0)
                        .build());

        int currentQuantity = inventory.getInStockQuantity() == null ? 0 : inventory.getInStockQuantity();
        inventory.setInStockQuantity(currentQuantity + receivedQty);
        internalWarehouseInventoryRepository.save(inventory);
    }

    private PurchaseOrder findInTransitOrder(String orderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay don dat hang: " + orderId));

        if (purchaseOrder.getStatus() != PurchaseOrder.PurchaseOrderStatus.IN_TRANSIT) {
            throw new RuntimeException("Chi co the doi soat don hang o trang thai IN_TRANSIT.");
        }

        return purchaseOrder;
    }

    private void validateSubmitDTO(ReconciliationSubmitDTO dto) {
        if (dto == null || dto.getLines() == null || dto.getLines().isEmpty()) {
            throw new RuntimeException("Du lieu doi soat khong duoc de trong.");
        }
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
        int receivedQty = line.getReceivedQty() == null ? 0 : line.getReceivedQty();
        return PartialOrderSelectionDTO.builder()
                .lineId(line.getId())
                .merchandiseCode(line.getMerchandise() == null ? "" : line.getMerchandise().getCode())
                .merchandiseName(line.getMerchandise() == null ? "" : line.getMerchandise().getName())
                .orderedQty(line.getOrderedQty())
                .receivedQty(receivedQty)
                .differenceQty(line.getOrderedQty() - receivedQty)
                .unit(line.getUnit())
                .build();
    }

    private String generateReportId() {
        return "BB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String normalizeText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
