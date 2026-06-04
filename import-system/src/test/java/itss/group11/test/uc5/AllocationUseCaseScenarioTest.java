package itss.group11.test.uc5;

import static itss.group11.test.uc5.AllocationServiceTestData.item;
import static itss.group11.test.uc5.AllocationServiceTestData.merchandise;
import static itss.group11.test.uc5.AllocationServiceTestData.pendingRequest;
import static itss.group11.test.uc5.AllocationServiceTestData.requestWithStatus;
import static itss.group11.test.uc5.AllocationServiceTestData.site;
import static itss.group11.test.uc5.AllocationServiceTestData.stock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import itss.group11.entity.uc5.AllocationPlanDTO;
import itss.group11.entity.uc5.AllocationResultDTO;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.entity.chung.PurchaseOrder;
import itss.group11.subsystem.chung.ImportSiteRepository;
import itss.group11.subsystem.chung.PurchaseOrderRepository;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.uc5.AllocationService;
import itss.group11.subsystem.uc5.InventoryCheckService;

@ExtendWith(MockitoExtension.class)
class AllocationUseCaseScenarioTest {

    @Mock
    private OrderRequestRepository orderRequestRepository;

    @Mock
    private ImportSiteRepository importSiteRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private InventoryCheckService inventoryCheckService;

    @InjectMocks
    private AllocationService allocationService;

    @Test
    void uc005_mainScenario_previewAndProcessSuccessfulAllocation() {
        Merchandise laptop = merchandise("MH001");
        OrderRequest request = pendingRequest("REQ201", item(laptop, 8));

        when(orderRequestRepository.findByRequestCode("REQ201")).thenReturn(Optional.of(request));
        when(inventoryCheckService.getStockDetailsAcrossSites("MH001"))
                .thenReturn(List.of(stock("SITE001", "MH001", 8, 14, 3)));
        when(inventoryCheckService.isStockSufficient("MH001", 8)).thenReturn(true);
        when(importSiteRepository.findBySiteCode("SITE001")).thenReturn(Optional.of(site("SITE001")));
        when(purchaseOrderRepository.existsByOrderId(anyString())).thenReturn(false);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AllocationPlanDTO preview = allocationService.previewAllocationPlan("REQ201");
        AllocationResultDTO result = allocationService.processAllocationPlan("REQ201");

        assertTrue(preview.isEnoughInventory());
        assertEquals(1, preview.getPlanItems().size());
        assertTrue(result.isSuccess());
        assertEquals(OrderRequest.OrderRequestStatus.ORDERED, request.getStatus());
        verify(orderRequestRepository).save(request);
        verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
    }

    @Test
    void uc005_alternativeScenario_insufficientInventoryStopsBeforeCreatingPurchaseOrder() {
        Merchandise laptop = merchandise("MH001");
        OrderRequest request = pendingRequest("REQ202", item(laptop, 50));

        when(orderRequestRepository.findByRequestCode("REQ202")).thenReturn(Optional.of(request));
        when(inventoryCheckService.isStockSufficient("MH001", 50)).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> allocationService.processAllocationPlan("REQ202")
        );

        assertEquals("INSUFFICIENT_INVENTORY", exception.getMessage());
        assertEquals(OrderRequest.OrderRequestStatus.PENDING, request.getStatus());
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void uc005_alternativeScenario_requestAlreadyProcessedCannotBeAllocatedAgain() {
        OrderRequest request = requestWithStatus(
                "REQ203",
                OrderRequest.OrderRequestStatus.ORDERED,
                List.of(item(merchandise("MH001"), 1))
        );

        when(orderRequestRepository.findByRequestCode("REQ203")).thenReturn(Optional.of(request));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> allocationService.processAllocationPlan("REQ203")
        );

        assertTrue(exception.getMessage().contains("khÃ´ng á»Ÿ tráº¡ng thÃ¡i PENDING"));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void uc005_alternativeScenario_oneRequestIsSplitIntoMinimumPurchaseOrdersBySite() {
        Merchandise laptop = merchandise("MH001");
        OrderRequest request = pendingRequest("REQ204", item(laptop, 12));

        when(orderRequestRepository.findByRequestCode("REQ204")).thenReturn(Optional.of(request));
        when(inventoryCheckService.isStockSufficient("MH001", 12)).thenReturn(true);
        when(inventoryCheckService.getStockDetailsAcrossSites("MH001")).thenReturn(List.of(
                stock("SITE001", "MH001", 7, 14, 3),
                stock("SITE002", "MH001", 5, 20, 4)
        ));
        when(importSiteRepository.findBySiteCode("SITE001")).thenReturn(Optional.of(site("SITE001")));
        when(importSiteRepository.findBySiteCode("SITE002")).thenReturn(Optional.of(site("SITE002")));
        when(purchaseOrderRepository.existsByOrderId(anyString())).thenReturn(false);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        allocationService.processAllocationPlan("REQ204");

        ArgumentCaptor<PurchaseOrder> poCaptor = ArgumentCaptor.forClass(PurchaseOrder.class);
        verify(purchaseOrderRepository, org.mockito.Mockito.times(2)).save(poCaptor.capture());

        List<PurchaseOrder> savedOrders = poCaptor.getAllValues();
        assertEquals(2, savedOrders.size());
        assertEquals(7, savedOrders.get(0).getOrderLines().get(0).getOrderedQty());
        assertEquals(5, savedOrders.get(1).getOrderLines().get(0).getOrderedQty());
    }
}

