package itss.group11.test.uc2;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import itss.group11.entity.chung.ImportSite;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.entity.chung.OrderRequestItem;
import itss.group11.entity.uc2.OrderRequestClassificationDTO;
import itss.group11.subsystem.chung.ImportSiteRepository;
import itss.group11.subsystem.chung.InventoryInquiryRepository;
import itss.group11.subsystem.chung.MerchandiseRepository;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.uc2.SiteSyncService;

class SiteSyncServiceBlackBoxTest {

    @Mock
    private MerchandiseRepository merchandiseRepository;

    @Mock
    private ImportSiteRepository importSiteRepository;

    @Mock
    private OrderRequestRepository orderRequestRepository;

    @Mock
    private InventoryInquiryRepository inventoryInquiryRepository;

    private SiteSyncService siteSyncService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        siteSyncService = new SiteSyncService(
                merchandiseRepository,
                importSiteRepository,
                orderRequestRepository,
                inventoryInquiryRepository
        );
    }

    @Test
    void classifyOrderRequest_validPendingRequestWithSite_returnsClassificationResult() {
        Merchandise merchandise = Merchandise.builder()
                .code("MH001")
                .name("Laptop Dell")
                .unit("Chiếc")
                .build();

        OrderRequestItem item = OrderRequestItem.builder()
                .id(1L)
                .merchandise(merchandise)
                .quantityOrdered(10)
                .build();

        OrderRequest request = OrderRequest.builder()
                .requestCode("REQ001")
                .status(OrderRequest.OrderRequestStatus.PENDING)
                .desiredDeliveryDate(LocalDate.of(2026, 6, 10))
                .items(List.of(item))
                .build();

        ImportSite site = ImportSite.builder()
                .siteCode("SITE001")
                .siteName("Tokyo Warehouse")
                .build();

        when(orderRequestRepository.findByRequestCode("REQ001"))
                .thenReturn(Optional.of(request));

        when(importSiteRepository.findSitesSellingMerchandise("MH001"))
                .thenReturn(List.of(site));

        when(inventoryInquiryRepository.countByRequestCodeAndSiteCode("REQ001", "SITE001"))
                .thenReturn(0L);

        OrderRequestClassificationDTO result =
                siteSyncService.classifyOrderRequest("REQ001");

        assertEquals("REQ001", result.getRequestCode());
        assertEquals(1, result.getItemCount());
        assertEquals(1, result.getSiteCount());
        assertEquals(1, result.getResults().size());
        assertEquals("SITE001", result.getResults().get(0).getSiteCode());
        assertEquals("Chờ gửi hỏi tồn kho", result.getResults().get(0).getStatus());
    }

    @Test
    void classifyOrderRequest_unknownRequestCode_throwsError() {
        when(orderRequestRepository.findByRequestCode("REQ404"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> siteSyncService.classifyOrderRequest("REQ404")
        );

        assertTrue(exception.getMessage().contains("Không tìm thấy"));
    }

    @Test
    void classifyOrderRequest_nonPendingRequest_throwsError() {
        OrderRequest request = OrderRequest.builder()
                .requestCode("REQ002")
                .status(OrderRequest.OrderRequestStatus.ORDERED)
                .items(List.of())
                .build();

        when(orderRequestRepository.findByRequestCode("REQ002"))
                .thenReturn(Optional.of(request));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> siteSyncService.classifyOrderRequest("REQ002")
        );

        assertTrue(exception.getMessage().contains("PENDING"));
    }

    @Test
    void classifyOrderRequest_pendingRequestWithoutItems_throwsError() {
        OrderRequest request = OrderRequest.builder()
                .requestCode("REQ003")
                .status(OrderRequest.OrderRequestStatus.PENDING)
                .items(List.of())
                .build();

        when(orderRequestRepository.findByRequestCode("REQ003"))
                .thenReturn(Optional.of(request));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> siteSyncService.classifyOrderRequest("REQ003")
        );

        assertTrue(exception.getMessage().contains("chưa có mặt hàng"));
    }
}