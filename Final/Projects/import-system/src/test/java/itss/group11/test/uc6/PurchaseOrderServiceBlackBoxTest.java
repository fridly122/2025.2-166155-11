package itss.group11.test.uc6;

import static itss.group11.test.uc6.Uc6TestData.ORDER_ID;
import static itss.group11.test.uc6.Uc6TestData.REPORT_ID;
import static itss.group11.test.uc6.Uc6TestData.inTransitOrder;
import static itss.group11.test.uc6.Uc6TestData.line;
import static itss.group11.test.uc6.Uc6TestData.merchandise;
import static itss.group11.test.uc6.Uc6TestData.purchaseOrderService;
import static itss.group11.test.uc6.Uc6TestData.receivedLine;
import static itss.group11.test.uc6.Uc6TestData.submit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import itss.group11.entity.chung.DiscrepancyReport;
import itss.group11.entity.chung.InternalWarehouseInventory;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.PurchaseOrder;
import itss.group11.entity.chung.PurchaseOrderLine;
import itss.group11.entity.uc6.ReconciliationDetailDTO;
import itss.group11.entity.uc6.ReconciliationResultDTO;
import itss.group11.subsystem.chung.DiscrepancyReportRepository;
import itss.group11.subsystem.chung.InternalWarehouseInventoryRepository;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.chung.PurchaseOrderLineRepository;
import itss.group11.subsystem.chung.PurchaseOrderRepository;
import itss.group11.subsystem.uc6.PurchaseOrderService;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceBlackBoxTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderLineRepository purchaseOrderLineRepository;

    @Mock
    private OrderRequestRepository orderRequestRepository;

    @Mock
    private InternalWarehouseInventoryRepository internalWarehouseInventoryRepository;

    @Mock
    private DiscrepancyReportRepository discrepancyReportRepository;

    private PurchaseOrderService service;
    private PurchaseOrder order;
    private Merchandise merchandise;
    private PurchaseOrderLine orderLine;

    @BeforeEach
    void setUp() {
        service = purchaseOrderService(
                purchaseOrderRepository,
                purchaseOrderLineRepository,
                orderRequestRepository,
                internalWarehouseInventoryRepository,
                discrepancyReportRepository
        );
        order = inTransitOrder();
        merchandise = merchandise("MH-001", "Hang nhap khau A", "cai", "125000.00");
        orderLine = line(1L, order, merchandise, 10, null);
    }

    @Test
    void getReconciliationDetail_validInTransitOrder_returnsOrderLinePriceAndDefaultReceivedQty() {
        mockOrderWithLines(List.of(orderLine));

        ReconciliationDetailDTO detail = service.getReconciliationDetail(ORDER_ID);

        assertEquals(ORDER_ID, detail.getOrderId());
        assertEquals("REQ-UC6-001", detail.getRequestCode());
        assertEquals("SITE-JP", detail.getSiteCode());
        assertEquals("IN_TRANSIT", detail.getStatus());
        assertEquals(1, detail.getLines().size());
        assertEquals("MH-001", detail.getLines().getFirst().getMerchandiseCode());
        assertEquals("Hang nhap khau A", detail.getLines().getFirst().getMerchandiseName());
        assertEquals(10, detail.getLines().getFirst().getOrderedQty());
        assertEquals(10, detail.getLines().getFirst().getReceivedQty());
        assertEquals(0, detail.getLines().getFirst().getDifferenceQty());
        assertEquals(0, new BigDecimal("125000.00").compareTo(detail.getLines().getFirst().getUnitPrice()));
    }

    @Test
    void reconcile_validMatchingQuantity_marksReceivedAndDoesNotCreateDiscrepancyReport() {
        mockOrderWithLines(List.of(orderLine));
        when(internalWarehouseInventoryRepository.findByMerchandise_Code("MH-001")).thenReturn(Optional.empty());
        when(internalWarehouseInventoryRepository.save(any(InternalWarehouseInventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReconciliationResultDTO result = service.reconcile(ORDER_ID, submit(receivedLine(1L, 10)));

        assertFalse(result.isHasDiscrepancy());
        assertEquals(List.of(), result.getDiscrepancyReportIds());
        assertEquals("RECEIVED", result.getStatus());
        assertEquals(PurchaseOrder.PurchaseOrderStatus.RECEIVED, order.getStatus());
        assertEquals(10, orderLine.getReceivedQty());

        ArgumentCaptor<InternalWarehouseInventory> inventoryCaptor =
                ArgumentCaptor.forClass(InternalWarehouseInventory.class);
        verify(internalWarehouseInventoryRepository).save(inventoryCaptor.capture());
        assertEquals("MH-001", inventoryCaptor.getValue().getMerchandise().getCode());
        assertEquals(10, inventoryCaptor.getValue().getInStockQuantity());

        verify(purchaseOrderLineRepository).save(orderLine);
        verify(purchaseOrderRepository).save(order);
        verify(discrepancyReportRepository, never()).save(any(DiscrepancyReport.class));
    }

    @Test
    void reconcile_validShortReceivedQuantity_createsDiscrepancyReportAndUpdatesExistingInventory() {
        InternalWarehouseInventory inventory = InternalWarehouseInventory.builder()
                .merchandise(merchandise)
                .inStockQuantity(3)
                .build();
        mockOrderWithLines(List.of(orderLine));
        when(internalWarehouseInventoryRepository.findByMerchandise_Code("MH-001")).thenReturn(Optional.of(inventory));
        when(internalWarehouseInventoryRepository.save(any(InternalWarehouseInventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(discrepancyReportRepository.save(any(DiscrepancyReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReconciliationResultDTO result = service.reconcile(ORDER_ID, submit(receivedLine(1L, 7)));

        assertTrue(result.isHasDiscrepancy());
        assertEquals(List.of(REPORT_ID), result.getDiscrepancyReportIds());
        assertEquals(PurchaseOrder.PurchaseOrderStatus.RECEIVED, order.getStatus());
        assertEquals(7, orderLine.getReceivedQty());
        assertEquals(10, inventory.getInStockQuantity());

        ArgumentCaptor<DiscrepancyReport> reportCaptor = ArgumentCaptor.forClass(DiscrepancyReport.class);
        verify(discrepancyReportRepository).save(reportCaptor.capture());
        DiscrepancyReport report = reportCaptor.getValue();
        assertEquals(REPORT_ID, report.getReportId());
        assertEquals(order, report.getPurchaseOrder());
        assertEquals(merchandise, report.getMerchandise());
        assertEquals(10, report.getOrderedQty());
        assertEquals(7, report.getReceivedQty());
        assertEquals(3, report.getDifferenceQty());
        assertEquals("Thieu hang khi doi soat", report.getReason());
        assertEquals("Kien hang bi rach niem phong", report.getNote());
        assertEquals("Nhan vien kho A", report.getCreatedBy());
    }

    @Test
    void reconcile_unknownOrderId_reportsNotFoundAndDoesNotUpdateInventory() {
        when(purchaseOrderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.reconcile(ORDER_ID, submit(receivedLine(1L, 10)))
        );

        assertTrue(exception.getMessage().contains("Khong tim thay don dat hang"));
        verifyNoInteractions(purchaseOrderLineRepository);
        verifyNoInteractions(internalWarehouseInventoryRepository);
        verifyNoInteractions(discrepancyReportRepository);
    }

    @Test
    void reconcile_orderNotInTransit_rejectsConfirmationBeforeLoadingLines() {
        order.setStatus(PurchaseOrder.PurchaseOrderStatus.CREATED);
        when(purchaseOrderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.reconcile(ORDER_ID, submit(receivedLine(1L, 10)))
        );

        assertTrue(exception.getMessage().contains("IN_TRANSIT"));
        verifyNoInteractions(purchaseOrderLineRepository);
        verifyNoInteractions(internalWarehouseInventoryRepository);
        verifyNoInteractions(discrepancyReportRepository);
    }

    @Test
    void reconcile_negativeReceivedQuantity_rejectsInputBeforeQueryingOrder() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.reconcile(ORDER_ID, submit(receivedLine(1L, -1)))
        );

        assertTrue(exception.getMessage().contains("So luong thuc nhan khong hop le"));
        verifyNoInteractions(purchaseOrderRepository);
        verifyNoInteractions(purchaseOrderLineRepository);
        verifyNoInteractions(internalWarehouseInventoryRepository);
        verifyNoInteractions(discrepancyReportRepository);
    }

    private void mockOrderWithLines(List<PurchaseOrderLine> lines) {
        when(purchaseOrderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));
        when(purchaseOrderLineRepository.findByPurchaseOrderOrderByIdAsc(order)).thenReturn(lines);
    }
}
