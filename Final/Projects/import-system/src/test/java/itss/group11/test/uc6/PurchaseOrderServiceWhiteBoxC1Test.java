package itss.group11.test.uc6;

import static itss.group11.test.uc6.Uc6TestData.ORDER_ID;
import static itss.group11.test.uc6.Uc6TestData.REPORT_ID;
import static itss.group11.test.uc6.Uc6TestData.inTransitOrder;
import static itss.group11.test.uc6.Uc6TestData.line;
import static itss.group11.test.uc6.Uc6TestData.merchandise;
import static itss.group11.test.uc6.Uc6TestData.purchaseOrderService;
import static itss.group11.test.uc6.Uc6TestData.receivedLine;
import static itss.group11.test.uc6.Uc6TestData.submit;
import static itss.group11.test.uc6.Uc6TestData.submitWithoutReportText;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import itss.group11.entity.chung.InternalWarehouseInventory;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.PurchaseOrder;
import itss.group11.entity.chung.PurchaseOrderLine;
import itss.group11.entity.uc6.PurchaseOrderResponseDTO;
import itss.group11.entity.uc6.ReconciliationDetailDTO;
import itss.group11.entity.uc6.ReconciliationSubmitDTO;
import itss.group11.subsystem.chung.DiscrepancyReportRepository;
import itss.group11.subsystem.chung.InternalWarehouseInventoryRepository;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.chung.PurchaseOrderLineRepository;
import itss.group11.subsystem.chung.PurchaseOrderRepository;
import itss.group11.subsystem.uc6.PurchaseOrderService;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceWhiteBoxC1Test {

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
    }

    @Test
    void reconcile_twoLinesCoversLoopBranchesForMatchedAndDiscrepantLines() {
        Merchandise firstMerchandise = merchandise("MH-001", "Hang A", "cai", "100000.00");
        Merchandise secondMerchandise = merchandise("MH-002", "Hang B", "hop", "50000.00");
        PurchaseOrderLine matchedLine = line(1L, order, firstMerchandise, 10, null);
        PurchaseOrderLine discrepantLine = line(2L, order, secondMerchandise, 5, null);
        InternalWarehouseInventory existingInventory = InternalWarehouseInventory.builder()
                .merchandise(firstMerchandise)
                .inStockQuantity(4)
                .build();

        mockOrderWithLines(List.of(matchedLine, discrepantLine));
        when(internalWarehouseInventoryRepository.findByMerchandise_Code("MH-001"))
                .thenReturn(Optional.of(existingInventory));
        when(internalWarehouseInventoryRepository.findByMerchandise_Code("MH-002"))
                .thenReturn(Optional.empty());
        when(internalWarehouseInventoryRepository.save(any(InternalWarehouseInventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(discrepancyReportRepository.save(any(DiscrepancyReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.reconcile(ORDER_ID, submit(receivedLine(1L, 10), receivedLine(2L, 2)));

        assertTrue(result.isHasDiscrepancy());
        assertEquals(List.of(REPORT_ID), result.getDiscrepancyReportIds());
        assertEquals(10, matchedLine.getReceivedQty());
        assertEquals(2, discrepantLine.getReceivedQty());
        assertEquals(14, existingInventory.getInStockQuantity());
        verify(purchaseOrderLineRepository, times(2)).save(any(PurchaseOrderLine.class));
        verify(internalWarehouseInventoryRepository, times(2)).save(any(InternalWarehouseInventory.class));
        verify(discrepancyReportRepository, times(1)).save(any(DiscrepancyReport.class));
        verify(purchaseOrderRepository).save(order);
    }

    @Test
    void reconcile_zeroReceivedQuantityCoversInventorySkipAndDefaultReportTextBranches() {
        Merchandise merchandise = merchandise("MH-003", "Hang C", "cai", "70000.00");
        PurchaseOrderLine line = line(3L, order, merchandise, 8, null);
        mockOrderWithLines(List.of(line));
        when(discrepancyReportRepository.save(any(DiscrepancyReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.reconcile(ORDER_ID, submitWithoutReportText(receivedLine(3L, 0)));

        verify(internalWarehouseInventoryRepository, never()).findByMerchandise_Code(any());
        verify(internalWarehouseInventoryRepository, never()).save(any(InternalWarehouseInventory.class));

        ArgumentCaptor<DiscrepancyReport> reportCaptor = ArgumentCaptor.forClass(DiscrepancyReport.class);
        verify(discrepancyReportRepository).save(reportCaptor.capture());
        assertEquals(8, reportCaptor.getValue().getDifferenceQty());
        assertEquals("Sai lech so luong khi doi soat nhap kho", reportCaptor.getValue().getReason());
        assertEquals("", reportCaptor.getValue().getNote());
        assertEquals("Nhan vien kho", reportCaptor.getValue().getCreatedBy());
    }

    @Test
    void reconcile_duplicateLineIdCoversDuplicateValidationBranchBeforeDbAccess() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.reconcile(ORDER_ID, submit(receivedLine(1L, 3), receivedLine(1L, 4)))
        );

        assertTrue(exception.getMessage().contains("bi trung"));
        verifyNoInteractions(purchaseOrderRepository);
        verifyNoInteractions(purchaseOrderLineRepository);
        verifyNoInteractions(internalWarehouseInventoryRepository);
        verifyNoInteractions(discrepancyReportRepository);
    }

    @Test
    void reconcile_emptyLineListCoversEmptyInputValidationBranchBeforeDbAccess() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.reconcile(ORDER_ID, new ReconciliationSubmitDTO(List.of(), "", "", ""))
        );

        assertTrue(exception.getMessage().contains("khong duoc de trong"));
        verifyNoInteractions(purchaseOrderRepository);
        verifyNoInteractions(purchaseOrderLineRepository);
        verifyNoInteractions(internalWarehouseInventoryRepository);
        verifyNoInteractions(discrepancyReportRepository);
    }

    @Test
    void reconcile_lineIdNotBelongingToOrderCoversOnlyExistingLinesValidationBranch() {
        Merchandise merchandise = merchandise("MH-004", "Hang D", "cai", "90000.00");
        PurchaseOrderLine line = line(4L, order, merchandise, 6, null);
        mockOrderWithLines(List.of(line));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.reconcile(ORDER_ID, submit(receivedLine(999L, 1)))
        );

        assertTrue(exception.getMessage().contains("khong thuoc don dat hang"));
        verifyNoInteractions(internalWarehouseInventoryRepository);
        verifyNoInteractions(discrepancyReportRepository);
        verify(purchaseOrderLineRepository, never()).save(any(PurchaseOrderLine.class));
        verify(purchaseOrderRepository, never()).save(order);
    }

    @Test
    void getReconciliationDetail_nullAssociationsCoverNullableMappingBranches() {
        order.setOrderRequest(null);
        order.setSite(null);
        PurchaseOrderLine lineWithoutMerchandise = line(5L, order, null, 4, null);
        mockOrderWithLines(List.of(lineWithoutMerchandise));

        ReconciliationDetailDTO detail = service.getReconciliationDetail(ORDER_ID);

        assertEquals("", detail.getRequestCode());
        assertEquals("", detail.getSiteCode());
        assertEquals("", detail.getSiteName());
        assertEquals("", detail.getLines().getFirst().getMerchandiseCode());
        assertEquals("", detail.getLines().getFirst().getMerchandiseName());
        assertEquals(4, detail.getLines().getFirst().getReceivedQty());
        assertEquals(0, detail.getLines().getFirst().getDifferenceQty());
        assertNull(detail.getLines().getFirst().getUnitPrice());
    }

    @Test
    void getInTransitOrders_coversCreatedDateAndNullableResponseMappingBranches() {
        PurchaseOrder fullOrder = inTransitOrder();
        PurchaseOrder nullableOrder = PurchaseOrder.builder()
                .orderId("PO-UC6-NULL")
                .status(PurchaseOrder.PurchaseOrderStatus.IN_TRANSIT)
                .createdAt(null)
                .orderRequest(null)
                .site(null)
                .build();
        when(purchaseOrderRepository.findByStatusOrderByCreatedAtDesc(PurchaseOrder.PurchaseOrderStatus.IN_TRANSIT))
                .thenReturn(List.of(fullOrder, nullableOrder));

        List<PurchaseOrderResponseDTO> orders = service.getInTransitOrders();

        assertEquals(2, orders.size());
        assertEquals("2026-06-06", orders.get(0).getCreatedDate());
        assertEquals("REQ-UC6-001", orders.get(0).getRequestCode());
        assertEquals("SITE-JP", orders.get(0).getSiteCode());
        assertEquals("", orders.get(1).getCreatedDate());
        assertEquals("", orders.get(1).getRequestCode());
        assertEquals("", orders.get(1).getSiteCode());
    }

    private void mockOrderWithLines(List<PurchaseOrderLine> lines) {
        when(purchaseOrderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));
        when(purchaseOrderLineRepository.findByPurchaseOrderOrderByIdAsc(order)).thenReturn(lines);
    }
}
