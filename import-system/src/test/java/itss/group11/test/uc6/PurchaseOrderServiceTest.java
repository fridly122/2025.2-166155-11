package itss.group11.test.uc6;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import itss.group11.entity.chung.DiscrepancyReport;
import itss.group11.entity.chung.ImportSite;
import itss.group11.entity.chung.InternalWarehouseInventory;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.entity.chung.PurchaseOrder;
import itss.group11.entity.chung.PurchaseOrderLine;
import itss.group11.entity.uc6.ReceivedLineDTO;
import itss.group11.entity.uc6.ReconciliationDetailDTO;
import itss.group11.entity.uc6.ReconciliationResultDTO;
import itss.group11.entity.uc6.ReconciliationSubmitDTO;
import itss.group11.subsystem.chung.DiscrepancyReportRepository;
import itss.group11.subsystem.chung.InternalWarehouseInventoryRepository;
import itss.group11.subsystem.chung.PurchaseOrderLineRepository;
import itss.group11.subsystem.chung.PurchaseOrderRepository;
import itss.group11.subsystem.uc6.DiscrepancyReportService;
import itss.group11.subsystem.uc6.PurchaseOrderService;
import itss.group11.subsystem.uc6.ReconciliationValidator;
import itss.group11.subsystem.uc6.WarehouseInventoryService;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    private static final String ORDER_ID = "PO-001";
    private static final String REPORT_ID = "BB-TEST-001";

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderLineRepository purchaseOrderLineRepository;

    @Mock
    private DiscrepancyReportRepository discrepancyReportRepository;

    @Mock
    private InternalWarehouseInventoryRepository internalWarehouseInventoryRepository;

    private PurchaseOrderService purchaseOrderService;
    private PurchaseOrder purchaseOrder;
    private PurchaseOrderLine purchaseOrderLine;
    private Merchandise merchandise;

    @BeforeEach
    void setUp() {
        WarehouseInventoryService warehouseInventoryService =
                new WarehouseInventoryService(internalWarehouseInventoryRepository);
        DiscrepancyReportService discrepancyReportService =
                new DiscrepancyReportService(discrepancyReportRepository, () -> REPORT_ID);

        purchaseOrderService = new PurchaseOrderService(
                purchaseOrderRepository,
                purchaseOrderLineRepository,
                new ReconciliationValidator(),
                warehouseInventoryService,
                discrepancyReportService
        );

        merchandise = Merchandise.builder()
                .code("MH-001")
                .name("Hang nhap khau A")
                .unit("cai")
                .build();

        purchaseOrder = PurchaseOrder.builder()
                .orderId(ORDER_ID)
                .status(PurchaseOrder.PurchaseOrderStatus.IN_TRANSIT)
                .orderRequest(OrderRequest.builder().requestCode("REQ-001").build())
                .site(ImportSite.builder().siteCode("SITE-JP").siteName("Tokyo Site").build())
                .build();

        purchaseOrderLine = PurchaseOrderLine.builder()
                .id(1L)
                .purchaseOrder(purchaseOrder)
                .merchandise(merchandise)
                .orderedQty(10)
                .receivedQty(0)
                .unit("cai")
                .build();
    }

    @Test
    void getReconciliationDetail_mapsPurchaseOrderAndLineData() {
        purchaseOrderLine.setReceivedQty(4);
        mockInTransitOrderWithLines(List.of(purchaseOrderLine));

        ReconciliationDetailDTO detail = purchaseOrderService.getReconciliationDetail(ORDER_ID);

        assertEquals(ORDER_ID, detail.getOrderId());
        assertEquals("REQ-001", detail.getRequestCode());
        assertEquals("SITE-JP", detail.getSiteCode());
        assertEquals(1, detail.getLines().size());
        assertEquals("MH-001", detail.getLines().getFirst().getMerchandiseCode());
        assertEquals(10, detail.getLines().getFirst().getOrderedQty());
        assertEquals(4, detail.getLines().getFirst().getReceivedQty());
        assertEquals(6, detail.getLines().getFirst().getDifferenceQty());
    }

    @Test
    void reconcile_whenQuantitiesMatch_marksReceivedAndUpdatesInventoryWithoutReport() {
        mockInTransitOrderWithLines(List.of(purchaseOrderLine));
        when(internalWarehouseInventoryRepository.findByMerchandise_Code("MH-001")).thenReturn(Optional.empty());
        when(internalWarehouseInventoryRepository.save(any(InternalWarehouseInventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReconciliationSubmitDTO dto = submitDto(new ReceivedLineDTO(1L, 10));

        ReconciliationResultDTO result = purchaseOrderService.reconcile(ORDER_ID, dto);

        assertFalse(result.isHasDiscrepancy());
        assertTrue(result.getDiscrepancyReportIds().isEmpty());
        assertEquals("RECEIVED", result.getStatus());
        assertEquals(PurchaseOrder.PurchaseOrderStatus.RECEIVED, purchaseOrder.getStatus());
        assertEquals(10, purchaseOrderLine.getReceivedQty());

        ArgumentCaptor<InternalWarehouseInventory> inventoryCaptor =
                ArgumentCaptor.forClass(InternalWarehouseInventory.class);
        verify(internalWarehouseInventoryRepository).save(inventoryCaptor.capture());
        assertEquals("MH-001", inventoryCaptor.getValue().getMerchandise().getCode());
        assertEquals(10, inventoryCaptor.getValue().getInStockQuantity());

        verify(purchaseOrderLineRepository).save(purchaseOrderLine);
        verify(purchaseOrderRepository).save(purchaseOrder);
        verify(discrepancyReportRepository, never()).save(any(DiscrepancyReport.class));
    }

    @Test
    void reconcile_whenQuantityDiffers_createsDiscrepancyReportAndAddsReceivedQuantityToInventory() {
        InternalWarehouseInventory inventory = InternalWarehouseInventory.builder()
                .merchandise(merchandise)
                .inStockQuantity(3)
                .build();

        mockInTransitOrderWithLines(List.of(purchaseOrderLine));
        when(internalWarehouseInventoryRepository.findByMerchandise_Code("MH-001")).thenReturn(Optional.of(inventory));
        when(internalWarehouseInventoryRepository.save(any(InternalWarehouseInventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(discrepancyReportRepository.save(any(DiscrepancyReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReconciliationResultDTO result = purchaseOrderService.reconcile(
                ORDER_ID,
                new ReconciliationSubmitDTO(
                        List.of(new ReceivedLineDTO(1L, 7)),
                        "Thieu 3 cai",
                        "Kien hang bi rach",
                        "Nhan vien kho A"
                )
        );

        assertTrue(result.isHasDiscrepancy());
        assertEquals(List.of(REPORT_ID), result.getDiscrepancyReportIds());
        assertEquals(7, purchaseOrderLine.getReceivedQty());
        assertEquals(10, inventory.getInStockQuantity());

        ArgumentCaptor<DiscrepancyReport> reportCaptor =
                ArgumentCaptor.forClass(DiscrepancyReport.class);
        verify(discrepancyReportRepository).save(reportCaptor.capture());

        DiscrepancyReport report = reportCaptor.getValue();
        assertEquals(REPORT_ID, report.getReportId());
        assertEquals(purchaseOrder, report.getPurchaseOrder());
        assertEquals(merchandise, report.getMerchandise());
        assertEquals(10, report.getOrderedQty());
        assertEquals(7, report.getReceivedQty());
        assertEquals(3, report.getDifferenceQty());
        assertEquals("Thieu 3 cai", report.getReason());
        assertEquals("Kien hang bi rach", report.getNote());
        assertEquals("Nhan vien kho A", report.getCreatedBy());
    }

    @Test
    void reconcile_rejectsOrderThatIsNotInTransit() {
        purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.CREATED);
        when(purchaseOrderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(purchaseOrder));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> purchaseOrderService.reconcile(ORDER_ID, submitDto(new ReceivedLineDTO(1L, 10)))
        );

        assertTrue(exception.getMessage().contains("IN_TRANSIT"));
        verifyNoInteractions(purchaseOrderLineRepository);
        verifyNoInteractions(internalWarehouseInventoryRepository);
        verifyNoInteractions(discrepancyReportRepository);
    }

    @Test
    void reconcile_rejectsNegativeReceivedQuantityBeforeLoadingOrder() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> purchaseOrderService.reconcile(ORDER_ID, submitDto(new ReceivedLineDTO(1L, -1)))
        );

        assertTrue(exception.getMessage().contains("So luong thuc nhan khong hop le"));
        verifyNoInteractions(purchaseOrderRepository);
    }

    @Test
    void reconcile_rejectsLineThatDoesNotBelongToOrder() {
        mockInTransitOrderWithLines(List.of(purchaseOrderLine));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> purchaseOrderService.reconcile(ORDER_ID, submitDto(new ReceivedLineDTO(99L, 1)))
        );

        assertTrue(exception.getMessage().contains("khong thuoc don dat hang"));
        verifyNoInteractions(internalWarehouseInventoryRepository);
        verifyNoInteractions(discrepancyReportRepository);
    }

    private void mockInTransitOrderWithLines(List<PurchaseOrderLine> lines) {
        when(purchaseOrderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderLineRepository.findByPurchaseOrderOrderByIdAsc(purchaseOrder)).thenReturn(lines);
    }

    private ReconciliationSubmitDTO submitDto(ReceivedLineDTO line) {
        return new ReconciliationSubmitDTO(List.of(line), "", "", "Nhan vien kho");
    }
}
