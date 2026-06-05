package itss.group11.test.uc1;

import static itss.group11.test.uc1.OrderRequestServiceTestData.creationDto;
import static itss.group11.test.uc1.OrderRequestServiceTestData.futureDate;
import static itss.group11.test.uc1.OrderRequestServiceTestData.line;
import static itss.group11.test.uc1.OrderRequestServiceTestData.merchandise;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.subsystem.chung.InventoryInquiryRepository;
import itss.group11.subsystem.chung.MerchandiseRepository;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.chung.PurchaseOrderRepository;
import itss.group11.subsystem.uc1.OrderRequestService;

@ExtendWith(MockitoExtension.class)
class OrderRequestServiceWhiteBoxC1Test {

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
    void createRequest_autoGenerateCodeUsesThreeDigitFormat_coversFormatBelow1000Branch() {
        Merchandise laptop = merchandise("MH001");
        OrderRequestCreationDTO dto = creationDto(" ", futureDate(7), line("MH001", 1));

        when(orderRequestRepository.findAllRequestCodes()).thenReturn(List.of("REQ001", "REQ002"));
        when(orderRequestRepository.existsById("REQ003")).thenReturn(false);
        when(merchandiseRepository.findByCode("MH001")).thenReturn(Optional.of(laptop));
        when(orderRequestRepository.save(any(OrderRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDetailDTO result = orderRequestService.createRequest(dto);

        assertEquals("REQ003", result.getRequestCode());
    }

    @Test
    void createRequest_autoGenerateCodeUsesPlainNumber_coversFormatAtLeast1000Branch() {
        Merchandise laptop = merchandise("MH001");
        OrderRequestCreationDTO dto = creationDto(null, futureDate(7), line("MH001", 1));

        when(orderRequestRepository.findAllRequestCodes()).thenReturn(List.of("REQ999"));
        when(orderRequestRepository.existsById("REQ1000")).thenReturn(false);
        when(merchandiseRepository.findByCode("MH001")).thenReturn(Optional.of(laptop));
        when(orderRequestRepository.save(any(OrderRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDetailDTO result = orderRequestService.createRequest(dto);

        assertEquals("REQ1000", result.getRequestCode());
    }

    @Test
    void createRequest_codeCollisionRetriesGeneration_coversWhileExistsLoopBranch() {
        Merchandise laptop = merchandise("MH001");
        OrderRequestCreationDTO dto = creationDto(null, futureDate(7), line("MH001", 1));

        when(orderRequestRepository.findAllRequestCodes()).thenReturn(List.of("REQ001"));
        when(orderRequestRepository.existsById("REQ002")).thenReturn(true);
        when(orderRequestRepository.existsById("REQ003")).thenReturn(false);
        when(merchandiseRepository.findByCode("MH001")).thenReturn(Optional.of(laptop));
        when(orderRequestRepository.save(any(OrderRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDetailDTO result = orderRequestService.createRequest(dto);

        assertEquals("REQ003", result.getRequestCode());
    }

    @Test
    void createRequest_nonStandardExistingCodesIgnored_coversExtractRequestNumberNoMatchBranch() {
        Merchandise laptop = merchandise("MH001");
        OrderRequestCreationDTO dto = creationDto(null, futureDate(7), line("MH001", 1));

        when(orderRequestRepository.findAllRequestCodes()).thenReturn(List.of("INVALID", "REQABC"));
        when(orderRequestRepository.existsById("REQ001")).thenReturn(false);
        when(merchandiseRepository.findByCode("MH001")).thenReturn(Optional.of(laptop));
        when(orderRequestRepository.save(any(OrderRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDetailDTO result = orderRequestService.createRequest(dto);

        assertEquals("REQ001", result.getRequestCode());
    }

    @Test
    void createRequest_trimAndUppercaseProvidedCode_coversNormalizeProvidedCodeBranch() {
        Merchandise laptop = merchandise("MH001");
        OrderRequestCreationDTO dto = creationDto(" req020 ", futureDate(7), line("mh001", 2));

        when(orderRequestRepository.existsById("REQ020")).thenReturn(false);
        when(merchandiseRepository.findByCode("MH001")).thenReturn(Optional.of(laptop));
        when(orderRequestRepository.save(any(OrderRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDetailDTO result = orderRequestService.createRequest(dto);

        assertEquals("REQ020", result.getRequestCode());
        assertEquals("MH001", result.getItems().get(0).getMerchandiseCode());
    }

    @Test
    void createRequest_nullQuantity_coversQuantityValidationBranch() {
        OrderRequestCreationDTO dto = creationDto("REQ021", futureDate(7), line("MH001", null));

        when(orderRequestRepository.existsById("REQ021")).thenReturn(false);
        when(merchandiseRepository.findByCode("MH001")).thenReturn(Optional.of(merchandise("MH001")));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderRequestService.createRequest(dto)
        );

        assertTrue(exception.getMessage().contains("Số lượng đặt phải lớn hơn 0"));
    }

    @Test
    void createRequest_blankDeliveryDate_coversRequireTextBranch() {
        OrderRequestCreationDTO dto = creationDto("REQ022", "   ", line("MH001", 1));

        when(orderRequestRepository.existsById("REQ022")).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderRequestService.createRequest(dto)
        );

        assertTrue(exception.getMessage().contains("Ngày mong muốn nhận hàng không được để trống"));
    }
}
