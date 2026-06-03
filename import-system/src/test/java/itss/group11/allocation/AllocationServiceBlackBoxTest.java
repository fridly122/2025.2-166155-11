package itss.group11.allocation;

import static itss.group11.allocation.AllocationServiceTestData.item;
import static itss.group11.allocation.AllocationServiceTestData.merchandise;
import static itss.group11.allocation.AllocationServiceTestData.pendingRequest;
import static itss.group11.allocation.AllocationServiceTestData.requestWithStatus;
import static itss.group11.allocation.AllocationServiceTestData.site;
import static itss.group11.allocation.AllocationServiceTestData.stock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import itss.group11.dto.allocation.AllocationResultDTO;
import itss.group11.models.Merchandise;
import itss.group11.models.OrderRequest;
import itss.group11.models.PurchaseOrder;
import itss.group11.models.PurchaseOrderLine;
import itss.group11.repository.allocation.ImportSiteRepository;
import itss.group11.repository.orderExecution.PurchaseOrderRepository;
import itss.group11.repository.requestManage.OrderRequestRepository;
import itss.group11.services.allocation.AllocationService;
import itss.group11.services.allocation.InventoryCheckService;

@ExtendWith(MockitoExtension.class)
class AllocationServiceBlackBoxTest {

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
    void processAllocationPlan_validPendingRequestWithEnoughInventory_createsPurchaseOrderAndMarksRequestOrdered() {
        Merchandise laptop = merchandise("MH001");
        OrderRequest request = pendingRequest("REQ001", item(laptop, 10));

        when(orderRequestRepository.findByRequestCode("REQ001")).thenReturn(Optional.of(request));
        when(inventoryCheckService.isStockSufficient("MH001", 10)).thenReturn(true);
        when(inventoryCheckService.getStockDetailsAcrossSites("MH001"))
                .thenReturn(List.of(stock("SITE001", "MH001", 10, 14, 3)));
        when(importSiteRepository.findBySiteCode("SITE001")).thenReturn(Optional.of(site("SITE001")));
        when(purchaseOrderRepository.existsByOrderId(anyString())).thenReturn(false);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AllocationResultDTO result = allocationService.processAllocationPlan("REQ001");

        assertTrue(result.isSuccess());
        assertEquals("REQ001", result.getRequestCode());
        assertEquals(OrderRequest.OrderRequestStatus.ORDERED, request.getStatus());
        assertEquals(1, result.getGeneratedPoCodes().size());

        ArgumentCaptor<PurchaseOrder> poCaptor = ArgumentCaptor.forClass(PurchaseOrder.class);
        verify(purchaseOrderRepository).save(poCaptor.capture());
        PurchaseOrder savedPo = poCaptor.getValue();

        assertEquals(PurchaseOrder.PurchaseOrderStatus.CREATED, savedPo.getStatus());
        assertEquals("SITE001", savedPo.getSite().getSiteCode());
        assertEquals(1, savedPo.getOrderLines().size());
        assertEquals(10, savedPo.getOrderLines().get(0).getOrderedQty());
        assertEquals(PurchaseOrderLine.DeliveryMeans.SHIP, savedPo.getOrderLines().get(0).getDeliveryMeans());
        verify(orderRequestRepository).save(request);
    }

    @Test
    void processAllocationPlan_unknownRequestCode_throwsNotFoundError() {
        when(orderRequestRepository.findByRequestCode("REQ404")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> allocationService.processAllocationPlan("REQ404")
        );

        assertTrue(exception.getMessage().contains("Không tìm thấy yêu cầu nhập hàng"));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void processAllocationPlan_nonPendingRequest_throwsInvalidStatusError() {
        OrderRequest orderedRequest = requestWithStatus(
                "REQ002",
                OrderRequest.OrderRequestStatus.ORDERED,
                List.of(item(merchandise("MH001"), 5))
        );
        when(orderRequestRepository.findByRequestCode("REQ002")).thenReturn(Optional.of(orderedRequest));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> allocationService.processAllocationPlan("REQ002")
        );

        assertTrue(exception.getMessage().contains("không ở trạng thái PENDING"));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void processAllocationPlan_pendingRequestWithoutItems_throwsNoItemError() {
        OrderRequest emptyRequest = requestWithStatus(
                "REQ003",
                OrderRequest.OrderRequestStatus.PENDING,
                List.of()
        );
        when(orderRequestRepository.findByRequestCode("REQ003")).thenReturn(Optional.of(emptyRequest));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> allocationService.processAllocationPlan("REQ003")
        );

        assertTrue(exception.getMessage().contains("chưa có mặt hàng"));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void processAllocationPlan_pendingRequestWithInsufficientInventory_throwsInsufficientInventory() {
        Merchandise laptop = merchandise("MH001");
        OrderRequest request = pendingRequest("REQ004", item(laptop, 20));

        when(orderRequestRepository.findByRequestCode("REQ004")).thenReturn(Optional.of(request));
        when(inventoryCheckService.isStockSufficient("MH001", 20)).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> allocationService.processAllocationPlan("REQ004")
        );

        assertEquals("INSUFFICIENT_INVENTORY", exception.getMessage());
        assertFalse(request.getStatus() == OrderRequest.OrderRequestStatus.ORDERED);
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
        verify(orderRequestRepository, never()).save(request);
    }
}
