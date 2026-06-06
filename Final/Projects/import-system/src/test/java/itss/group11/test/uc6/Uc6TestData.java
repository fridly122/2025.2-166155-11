package itss.group11.test.uc6;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import itss.group11.entity.chung.ImportSite;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.entity.chung.PurchaseOrder;
import itss.group11.entity.chung.PurchaseOrderLine;
import itss.group11.entity.uc6.ReceivedLineDTO;
import itss.group11.entity.uc6.ReconciliationSubmitDTO;
import itss.group11.subsystem.chung.DiscrepancyReportRepository;
import itss.group11.subsystem.chung.InternalWarehouseInventoryRepository;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.chung.PurchaseOrderLineRepository;
import itss.group11.subsystem.chung.PurchaseOrderRepository;
import itss.group11.subsystem.uc6.DiscrepancyReportService;
import itss.group11.subsystem.uc6.PurchaseOrderService;
import itss.group11.subsystem.uc6.ReconciliationValidator;
import itss.group11.subsystem.uc6.WarehouseInventoryService;

final class Uc6TestData {

    static final String ORDER_ID = "PO-UC6-001";
    static final String REQUEST_CODE = "REQ-UC6-001";
    static final String SITE_CODE = "SITE-JP";
    static final String SITE_NAME = "Tokyo Import Site";
    static final String REPORT_ID = "BB-UC6-001";

    private Uc6TestData() {
    }

    static PurchaseOrderService purchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderLineRepository purchaseOrderLineRepository,
            OrderRequestRepository orderRequestRepository,
            InternalWarehouseInventoryRepository internalWarehouseInventoryRepository,
            DiscrepancyReportRepository discrepancyReportRepository
    ) {
        WarehouseInventoryService warehouseInventoryService =
                new WarehouseInventoryService(internalWarehouseInventoryRepository);
        DiscrepancyReportService discrepancyReportService =
                new DiscrepancyReportService(discrepancyReportRepository, () -> REPORT_ID);

        return new PurchaseOrderService(
                purchaseOrderRepository,
                purchaseOrderLineRepository,
                orderRequestRepository,
                new ReconciliationValidator(),
                warehouseInventoryService,
                discrepancyReportService
        );
    }

    static PurchaseOrder inTransitOrder() {
        return orderWithStatus(PurchaseOrder.PurchaseOrderStatus.IN_TRANSIT);
    }

    static PurchaseOrder orderWithStatus(PurchaseOrder.PurchaseOrderStatus status) {
        return PurchaseOrder.builder()
                .orderId(ORDER_ID)
                .status(status)
                .createdAt(LocalDateTime.of(2026, 6, 6, 9, 0))
                .orderRequest(OrderRequest.builder().requestCode(REQUEST_CODE).build())
                .site(ImportSite.builder().siteCode(SITE_CODE).siteName(SITE_NAME).build())
                .build();
    }

    static Merchandise merchandise(String code, String name, String unit, String unitPrice) {
        return Merchandise.builder()
                .code(code)
                .name(name)
                .unit(unit)
                .unitPrice(new BigDecimal(unitPrice))
                .build();
    }

    static PurchaseOrderLine line(Long id, PurchaseOrder order, Merchandise merchandise, int orderedQty, Integer receivedQty) {
        return PurchaseOrderLine.builder()
                .id(id)
                .purchaseOrder(order)
                .merchandise(merchandise)
                .orderedQty(orderedQty)
                .receivedQty(receivedQty)
                .unit(merchandise == null ? "cai" : merchandise.getUnit())
                .build();
    }

    static ReceivedLineDTO receivedLine(Long lineId, Integer receivedQty) {
        return new ReceivedLineDTO(lineId, receivedQty);
    }

    static ReconciliationSubmitDTO submit(ReceivedLineDTO... lines) {
        return new ReconciliationSubmitDTO(
                List.of(lines),
                "Thieu hang khi doi soat",
                "Kien hang bi rach niem phong",
                "Nhan vien kho A"
        );
    }

    static ReconciliationSubmitDTO submitWithoutReportText(ReceivedLineDTO... lines) {
        return new ReconciliationSubmitDTO(List.of(lines), "", "", "");
    }
}
