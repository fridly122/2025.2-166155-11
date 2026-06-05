package itss.group11.test.uc5;

import static itss.group11.test.uc5.AllocationServiceTestData.item;
import static itss.group11.test.uc5.AllocationServiceTestData.merchandise;
import static itss.group11.test.uc5.AllocationServiceTestData.pendingRequest;
import static itss.group11.test.uc5.AllocationServiceTestData.site;
import static itss.group11.test.uc5.AllocationServiceTestData.stock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.entity.chung.PurchaseOrder;
import itss.group11.entity.chung.PurchaseOrderLine;
import itss.group11.subsystem.chung.ImportSiteRepository;
import itss.group11.subsystem.chung.PurchaseOrderRepository;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.uc5.AllocationService;
import itss.group11.subsystem.uc5.InventoryCheckService;

@ExtendWith(MockitoExtension.class)
class AllocationServiceWhiteBoxC1Test {

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
    void previewAllocationPlan_multipleStocksCoversLoopBreakEnoughInventoryShipAndAirBranches() {
        Merchandise laptop = merchandise("MH001");
        OrderRequest request = pendingRequest("REQ101", item(laptop, 5));

        when(orderRequestRepository.findByRequestCode("REQ101")).thenReturn(Optional.of(request));
        when(inventoryCheckService.getStockDetailsAcrossSites("MH001")).thenReturn(List.of(
                stock("SITE001", "MH001", 3, 14, 3),
                stock("SITE002", "MH001", 2, null, 4),
                stock("SITE003", "MH001", 100, 10, 2)
        ));

        AllocationPlanDTO plan = allocationService.previewAllocationPlan("REQ101");

        assertTrue(plan.isEnoughInventory());
        assertEquals("Kế hoạch khả thi.", plan.getMessage());
        assertEquals(2, plan.getPlanItems().size());
        assertEquals("SHIP", plan.getPlanItems().get(0).getDeliveryMeans());
        assertEquals("AIR", plan.getPlanItems().get(1).getDeliveryMeans());
        assertEquals(3, plan.getPlanItems().get(0).getAllocatedQuantity());
        assertEquals(2, plan.getPlanItems().get(1).getAllocatedQuantity());
    }

    @Test
    void previewAllocationPlan_remainingQuantityAfterAllStocksCoversInsufficientInventoryBranch() {
        Merchandise laptop = merchandise("MH001");
        OrderRequest request = pendingRequest("REQ102", item(laptop, 10));

        when(orderRequestRepository.findByRequestCode("REQ102")).thenReturn(Optional.of(request));
        when(inventoryCheckService.getStockDetailsAcrossSites("MH001"))
                .thenReturn(List.of(stock("SITE001", "MH001", 4, 14, 3)));

        AllocationPlanDTO plan = allocationService.previewAllocationPlan("REQ102");

        assertTrue(!plan.isEnoughInventory());
        assertEquals("Cảnh báo: Không đủ tồn kho!", plan.getMessage());
        assertEquals(1, plan.getPlanItems().size());
        assertEquals(4, plan.getPlanItems().get(0).getAllocatedQuantity());
    }

    @Test
    void previewAllocationPlan_siteWithoutTransportDaysCoversTransportErrorBranch() {
        Merchandise laptop = merchandise("MH001");
        OrderRequest request = pendingRequest("REQ103", item(laptop, 1));

        when(orderRequestRepository.findByRequestCode("REQ103")).thenReturn(Optional.of(request));
        when(inventoryCheckService.getStockDetailsAcrossSites("MH001"))
                .thenReturn(List.of(stock("SITE001", "MH001", 1, null, null)));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> allocationService.previewAllocationPlan("REQ103")
        );

        assertTrue(exception.getMessage().contains("chua co du lieu ngay van chuyen"));
    }

    @Test
    void processAllocationPlan_twoItemsAllocatedToSameSiteCoversPurchaseOrderGroupingBranch() {
        Merchandise laptop = merchandise("MH001");
        Merchandise mouse = merchandise("MH002");
        OrderRequest request = pendingRequest("REQ104", item(laptop, 3), item(mouse, 7));

        when(orderRequestRepository.findByRequestCode("REQ104")).thenReturn(Optional.of(request));
        when(inventoryCheckService.isStockSufficient("MH001", 3)).thenReturn(true);
        when(inventoryCheckService.isStockSufficient("MH002", 7)).thenReturn(true);
        when(inventoryCheckService.getStockDetailsAcrossSites("MH001"))
                .thenReturn(List.of(stock("SITE001", "MH001", 3, 14, 3)));
        when(inventoryCheckService.getStockDetailsAcrossSites("MH002"))
                .thenReturn(List.of(stock("SITE001", "MH002", 7, 14, 3)));
        when(importSiteRepository.findBySiteCode("SITE001")).thenReturn(Optional.of(site("SITE001")));
        when(purchaseOrderRepository.existsByOrderId(anyString())).thenReturn(false);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        allocationService.processAllocationPlan("REQ104");

        ArgumentCaptor<PurchaseOrder> poCaptor = ArgumentCaptor.forClass(PurchaseOrder.class);
        org.mockito.Mockito.verify(purchaseOrderRepository).save(poCaptor.capture());
        PurchaseOrder savedPo = poCaptor.getValue();

        assertEquals(1, org.mockito.Mockito.mockingDetails(importSiteRepository).getInvocations()
                .stream()
                .filter(invocation -> invocation.getMethod().getName().equals("findBySiteCode"))
                .count());
        assertEquals(2, savedPo.getOrderLines().size());
        assertEquals(PurchaseOrderLine.DeliveryMeans.SHIP, savedPo.getOrderLines().get(0).getDeliveryMeans());
        assertEquals(PurchaseOrderLine.DeliveryMeans.SHIP, savedPo.getOrderLines().get(1).getDeliveryMeans());
    }
}

