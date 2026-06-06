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
import itss.group11.entity.uc6.ReconciliationResultDTO;
import itss.group11.subsystem.chung.DiscrepancyReportRepository;
import itss.group11.subsystem.chung.InternalWarehouseInventoryRepository;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.chung.PurchaseOrderLineRepository;
import itss.group11.subsystem.chung.PurchaseOrderRepository;
import itss.group11.subsystem.uc6.PurchaseOrderService;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderUseCaseScenarioTest {

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
    void uc006_mainScenario_confirmReceivingWithoutDiscrepancy() {
        Merchandise keyboard = merchandise("MH-101", "Ban phim co", "cai", "450000.00");
        Merchandise mouse = merchandise("MH-102", "Chuot khong day", "cai", "250000.00");
        PurchaseOrderLine firstLine = line(101L, order, keyboard, 5, null);
        PurchaseOrderLine secondLine = line(102L, order, mouse, 8, null);
        mockOrderWithLines(List.of(firstLine, secondLine));
        when(internalWarehouseInventoryRepository.findByMerchandise_Code("MH-101")).thenReturn(Optional.empty());
        when(internalWarehouseInventoryRepository.findByMerchandise_Code("MH-102")).thenReturn(Optional.empty());
        when(internalWarehouseInventoryRepository.save(any(InternalWarehouseInventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReconciliationResultDTO result = service.reconcile(
                ORDER_ID,
                submit(receivedLine(101L, 5), receivedLine(102L, 8))
        );

        assertEquals("RECEIVED", result.getStatus());
        assertTrue(!result.isHasDiscrepancy());
        assertEquals(PurchaseOrder.PurchaseOrderStatus.RECEIVED, order.getStatus());
        verify(purchaseOrderLineRepository, times(2)).save(any(PurchaseOrderLine.class));
        verify(internalWarehouseInventoryRepository, times(2)).save(any(InternalWarehouseInventory.class));
        verify(discrepancyReportRepository, never()).save(any(DiscrepancyReport.class));
        verify(purchaseOrderRepository).save(order);
    }

    @Test
    void uc006_alternativeScenario_confirmReceivingWithDiscrepancyCreatesReport() {
        Merchandise monitor = merchandise("MH-201", "Man hinh", "cai", "3200000.00");
        PurchaseOrderLine line = line(201L, order, monitor, 4, null);
        mockOrderWithLines(List.of(line));
        when(internalWarehouseInventoryRepository.findByMerchandise_Code("MH-201")).thenReturn(Optional.empty());
        when(internalWarehouseInventoryRepository.save(any(InternalWarehouseInventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(discrepancyReportRepository.save(any(DiscrepancyReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReconciliationResultDTO result = service.reconcile(ORDER_ID, submit(receivedLine(201L, 3)));

        assertTrue(result.isHasDiscrepancy());
        assertEquals(List.of(REPORT_ID), result.getDiscrepancyReportIds());
        assertEquals(PurchaseOrder.PurchaseOrderStatus.RECEIVED, order.getStatus());

        ArgumentCaptor<DiscrepancyReport> reportCaptor = ArgumentCaptor.forClass(DiscrepancyReport.class);
        verify(discrepancyReportRepository).save(reportCaptor.capture());
        assertEquals(1, reportCaptor.getValue().getDifferenceQty());
        assertEquals("Thieu hang khi doi soat", reportCaptor.getValue().getReason());
    }

    @Test
    void uc006_exceptionScenario_orderAlreadyReceivedCannotBeConfirmedAgain() {
        order.setStatus(PurchaseOrder.PurchaseOrderStatus.RECEIVED);
        when(purchaseOrderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.reconcile(ORDER_ID, submit(receivedLine(301L, 1)))
        );

        assertTrue(exception.getMessage().contains("IN_TRANSIT"));
        verifyNoInteractions(purchaseOrderLineRepository);
        verifyNoInteractions(internalWarehouseInventoryRepository);
        verifyNoInteractions(discrepancyReportRepository);
        verify(purchaseOrderRepository, never()).save(order);
    }

    @Test
    void uc006_exceptionScenario_receivedLineNotBelongingToSelectedOrderStopsBeforeUpdatingStock() {
        Merchandise camera = merchandise("MH-301", "Camera", "cai", "1800000.00");
        PurchaseOrderLine orderLine = line(301L, order, camera, 2, null);
        mockOrderWithLines(List.of(orderLine));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.reconcile(ORDER_ID, submit(receivedLine(999L, 1)))
        );

        assertTrue(exception.getMessage().contains("khong thuoc don dat hang"));
        assertEquals(PurchaseOrder.PurchaseOrderStatus.IN_TRANSIT, order.getStatus());
        verifyNoInteractions(internalWarehouseInventoryRepository);
        verifyNoInteractions(discrepancyReportRepository);
        verify(purchaseOrderLineRepository, never()).save(any(PurchaseOrderLine.class));
        verify(purchaseOrderRepository, never()).save(order);
    }

    private void mockOrderWithLines(List<PurchaseOrderLine> lines) {
        when(purchaseOrderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));
        when(purchaseOrderLineRepository.findByPurchaseOrderOrderByIdAsc(order)).thenReturn(lines);
    }
}
