package itss.group11.test.uc1;

import static itss.group11.test.uc1.OrderRequestServiceTestData.creationDto;
import static itss.group11.test.uc1.OrderRequestServiceTestData.futureDate;
import static itss.group11.test.uc1.OrderRequestServiceTestData.line;
import static itss.group11.test.uc1.OrderRequestServiceTestData.merchandise;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import itss.group11.entity.uc1.OrderRequestCreationDTO;
import itss.group11.entity.uc1.OrderRequestDetailDTO;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.subsystem.chung.InventoryInquiryRepository;
import itss.group11.subsystem.chung.MerchandiseRepository;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.chung.PurchaseOrderRepository;
import itss.group11.subsystem.uc1.OrderRequestService;

@ExtendWith(MockitoExtension.class)
class OrderRequestServiceBlackBoxTest {

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
    void createRequest_validDataWithExplicitCode_returnsPendingDetail() {
        Merchandise laptop = merchandise("MH001");
        OrderRequestCreationDTO dto = creationDto("REQ010", futureDate(7), line("MH001", 5));

        when(orderRequestRepository.existsById("REQ010")).thenReturn(false);
        when(merchandiseRepository.findByCode("MH001")).thenReturn(Optional.of(laptop));
        when(orderRequestRepository.save(any(OrderRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDetailDTO result = orderRequestService.createRequest(dto);

        assertEquals("REQ010", result.getRequestCode());
        assertEquals("PENDING", result.getStatus());
        assertEquals(1, result.getItems().size());
        assertEquals(5, result.getItems().get(0).getQuantityOrdered());

        ArgumentCaptor<OrderRequest> captor = ArgumentCaptor.forClass(OrderRequest.class);
        verify(orderRequestRepository).save(captor.capture());
        assertEquals(OrderRequest.OrderRequestStatus.PENDING, captor.getValue().getStatus());
    }

    @Test
    void createRequest_blankRequestCode_generatesUniqueCode() {
        Merchandise laptop = merchandise("MH001");
        OrderRequestCreationDTO dto = creationDto(null, futureDate(7), line("MH001", 2));

        when(orderRequestRepository.findAllRequestCodes()).thenReturn(java.util.List.of("REQ001", "REQ005"));
        when(orderRequestRepository.existsById("REQ006")).thenReturn(false);
        when(merchandiseRepository.findByCode("MH001")).thenReturn(Optional.of(laptop));
        when(orderRequestRepository.save(any(OrderRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDetailDTO result = orderRequestService.createRequest(dto);

        assertEquals("REQ006", result.getRequestCode());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void createRequest_duplicateRequestCode_throwsError() {
        OrderRequestCreationDTO dto = creationDto("REQ010", futureDate(7), line("MH001", 2));

        when(orderRequestRepository.existsById("REQ010")).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderRequestService.createRequest(dto)
        );

        assertTrue(exception.getMessage().contains("Mã yêu cầu đã tồn tại"));
        verify(orderRequestRepository, never()).save(any(OrderRequest.class));
    }

    @Test
    void createRequest_nullDto_throwsError() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderRequestService.createRequest(null)
        );

        assertTrue(exception.getMessage().contains("Dữ liệu tạo yêu cầu không được để trống"));
    }

    @Test
    void createRequest_emptyItems_throwsError() {
        OrderRequestCreationDTO dto = creationDto("REQ011", futureDate(7));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderRequestService.createRequest(dto)
        );

        assertTrue(exception.getMessage().contains("phải có ít nhất một mặt hàng"));
    }

    @Test
    void createRequest_unknownMerchandise_throwsError() {
        OrderRequestCreationDTO dto = creationDto("REQ012", futureDate(7), line("MH404", 1));

        when(orderRequestRepository.existsById("REQ012")).thenReturn(false);
        when(merchandiseRepository.findByCode("MH404")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderRequestService.createRequest(dto)
        );

        assertTrue(exception.getMessage().contains("Không tìm thấy mặt hàng"));
    }

    @Test
    void createRequest_zeroQuantity_throwsError() {
        OrderRequestCreationDTO dto = creationDto("REQ013", futureDate(7), line("MH001", 0));

        when(orderRequestRepository.existsById("REQ013")).thenReturn(false);
        when(merchandiseRepository.findByCode("MH001")).thenReturn(Optional.of(merchandise("MH001")));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderRequestService.createRequest(dto)
        );

        assertTrue(exception.getMessage().contains("Số lượng đặt phải lớn hơn 0"));
    }

    @Test
    void createRequest_pastDeliveryDate_throwsError() {
        OrderRequestCreationDTO dto = creationDto(
                "REQ014",
                LocalDate.now().toString(),
                line("MH001", 1)
        );

        when(orderRequestRepository.existsById("REQ014")).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderRequestService.createRequest(dto)
        );

        assertTrue(exception.getMessage().contains("phải sau ngày hiện tại"));
    }

    @Test
    void createRequest_invalidDateFormat_throwsError() {
        OrderRequestCreationDTO dto = creationDto("REQ015", "31/12/2026", line("MH001", 1));

        when(orderRequestRepository.existsById("REQ015")).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderRequestService.createRequest(dto)
        );

        assertTrue(exception.getMessage().contains("yyyy-MM-dd"));
    }
}
