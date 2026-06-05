package itss.group11.test.uc1;

import static itss.group11.test.uc1.OrderRequestServiceTestData.creationDto;
import static itss.group11.test.uc1.OrderRequestServiceTestData.futureDate;
import static itss.group11.test.uc1.OrderRequestServiceTestData.item;
import static itss.group11.test.uc1.OrderRequestServiceTestData.line;
import static itss.group11.test.uc1.OrderRequestServiceTestData.merchandise;
import static itss.group11.test.uc1.OrderRequestServiceTestData.pendingRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import itss.group11.entity.uc1.OrderRequestCreationDTO;
import itss.group11.entity.uc1.OrderRequestDetailDTO;
import itss.group11.entity.uc1.OrderRequestSummaryDTO;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.subsystem.chung.InventoryInquiryRepository;
import itss.group11.subsystem.chung.MerchandiseRepository;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.chung.PurchaseOrderRepository;
import itss.group11.subsystem.uc1.OrderRequestService;

@ExtendWith(MockitoExtension.class)
class OrderRequestUseCaseScenarioTest {

    @Mock
    private OrderRequestRepository orderRequestRepository;

    @Mock
    private MerchandiseRepository merchandiseRepository;

    @Mock
    private InventoryInquiryRepository inventoryInquiryRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @InjectMocks
    private OrderRequestService orderRequestService;

    @Test
    void uc001_mainScenario_salesCreatesPendingOrderRequest() {
        Merchandise laptop = merchandise("MH001");
        OrderRequestCreationDTO dto = creationDto("REQ201", futureDate(10), line("MH001", 8));

        when(orderRequestRepository.existsById("REQ201")).thenReturn(false);
        when(merchandiseRepository.findByCode("MH001")).thenReturn(Optional.of(laptop));
        when(orderRequestRepository.save(any(OrderRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDetailDTO result = orderRequestService.createRequest(dto);

        assertEquals("REQ201", result.getRequestCode());
        assertEquals("PENDING", result.getStatus());
        assertEquals(futureDate(10), result.getDesiredDeliveryDate());
        assertEquals(1, result.getItems().size());
        verify(orderRequestRepository).save(any(OrderRequest.class));
    }

    @Test
    void uc001_alternativeScenario_invalidMerchandiseRejected() {
        OrderRequestCreationDTO dto = creationDto("REQ202", futureDate(5), line("MH999", 3));

        when(orderRequestRepository.existsById("REQ202")).thenReturn(false);
        when(merchandiseRepository.findByCode("MH999")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderRequestService.createRequest(dto)
        );

        assertTrue(exception.getMessage().contains("Không tìm thấy mặt hàng"));
        verify(orderRequestRepository, never()).save(any(OrderRequest.class));
    }

    @Test
    void uc001_alternativeScenario_invalidDeliveryDateRejected() {
        OrderRequestCreationDTO dto = creationDto("REQ203", "2020-01-01", line("MH001", 1));

        when(orderRequestRepository.existsById("REQ203")).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderRequestService.createRequest(dto)
        );

        assertTrue(exception.getMessage().contains("phải sau ngày hiện tại"));
    }

    @Test
    void uc001_alternativeScenario_searchRequestByCode() {
        OrderRequest request = pendingRequest("REQ204", item(merchandise("MH001"), 2));

        when(orderRequestRepository.findByRequestCodeContainingIgnoreCaseOrderByCreatedAtAsc("204"))
                .thenReturn(List.of(request));

        List<OrderRequestSummaryDTO> results = orderRequestService.searchRequests("204");

        assertEquals(1, results.size());
        assertEquals("REQ204", results.get(0).getRequestCode());
        assertEquals("PENDING", results.get(0).getStatus());
    }

    @Test
    void uc001_alternativeScenario_updatePendingRequest() {
        Merchandise laptop = merchandise("MH001");
        OrderRequest existing = pendingRequest("REQ205", item(laptop, 2));
        OrderRequestCreationDTO dto = creationDto("REQ205", futureDate(14), line("MH001", 5));

        when(orderRequestRepository.findByRequestCode("REQ205")).thenReturn(Optional.of(existing));
        when(merchandiseRepository.findByCode("MH001")).thenReturn(Optional.of(laptop));
        when(orderRequestRepository.save(any(OrderRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDetailDTO result = orderRequestService.updateRequest("REQ205", dto);

        assertEquals(5, result.getItems().get(0).getQuantityOrdered());
        assertEquals(futureDate(14), result.getDesiredDeliveryDate());
    }

    @Test
    void uc001_alternativeScenario_deletePendingRequestWithoutInquiry() {
        OrderRequest existing = pendingRequest("REQ206", item(merchandise("MH001"), 1));

        when(orderRequestRepository.findByRequestCode("REQ206")).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.existsByOrderRequest_RequestCode("REQ206")).thenReturn(false);
        when(inventoryInquiryRepository.existsByOrderRequest_RequestCode("REQ206")).thenReturn(false);

        String message = orderRequestService.deleteRequest("REQ206");

        assertTrue(message.contains("Đã xóa yêu cầu nhập hàng"));
        verify(orderRequestRepository).delete(existing);
    }

    @Test
    void uc001_alternativeScenario_cancelRequestWithInventoryInquiry() {
        OrderRequest existing = pendingRequest("REQ207", item(merchandise("MH001"), 1));

        when(orderRequestRepository.findByRequestCode("REQ207")).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.existsByOrderRequest_RequestCode("REQ207")).thenReturn(false);
        when(inventoryInquiryRepository.existsByOrderRequest_RequestCode("REQ207")).thenReturn(true);
        when(orderRequestRepository.save(any(OrderRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String message = orderRequestService.deleteRequest("REQ207");

        assertTrue(message.contains("CANCELLED"));
        assertEquals(OrderRequest.OrderRequestStatus.CANCELLED, existing.getStatus());
        verify(orderRequestRepository, never()).delete(existing);
        verify(orderRequestRepository).save(existing);
    }
}
