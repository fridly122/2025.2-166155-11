package itss.group11.test.uc2;

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

class SiteSyncServiceWhiteBoxC1Test {

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
    void classifyOrderRequest_blankRequestCode_coversInvalidInputBranch() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> siteSyncService.classifyOrderRequest("")
        );

        assertTrue(exception.getMessage().contains("không được để trống"));
    }

    @Test
    void classifyOrderRequest_itemWithoutSite_coversNoSiteBranch() {
        Merchandise merchandise = Merchandise.builder()
                .code("MH002")
                .name("Mouse")
                .unit("Chiếc")
                .build();

        OrderRequestItem item = OrderRequestItem.builder()
                .id(1L)
                .merchandise(merchandise)
                .quantityOrdered(5)
                .build();

        OrderRequest request = OrderRequest.builder()
                .requestCode("REQ010")
                .status(OrderRequest.OrderRequestStatus.PENDING)
                .items(List.of(item))
                .build();

        when(orderRequestRepository.findByRequestCode("REQ010"))
                .thenReturn(Optional.of(request));

        when(importSiteRepository.findSitesSellingMerchandise("MH002"))
                .thenReturn(List.of());

        OrderRequestClassificationDTO result =
                siteSyncService.classifyOrderRequest("REQ010");

        assertEquals(0, result.getSiteCount());
        assertTrue(result.getResults().isEmpty());
        assertTrue(result.getMessage().contains("Không tìm thấy"));
    }

    @Test
    void classifyOrderRequest_alreadySentInquiry_coversSentStatusBranch() {
        Merchandise merchandise = Merchandise.builder()
                .code("MH003")
                .name("Keyboard")
                .unit("Chiếc")
                .build();

        OrderRequestItem item = OrderRequestItem.builder()
                .id(1L)
                .merchandise(merchandise)
                .quantityOrdered(3)
                .build();

        OrderRequest request = OrderRequest.builder()
                .requestCode("REQ011")
                .status(OrderRequest.OrderRequestStatus.PENDING)
                .items(List.of(item))
                .build();

        ImportSite site = ImportSite.builder()
                .siteCode("SITE003")
                .siteName("Shanghai Warehouse")
                .build();

        when(orderRequestRepository.findByRequestCode("REQ011"))
                .thenReturn(Optional.of(request));

        when(importSiteRepository.findSitesSellingMerchandise("MH003"))
                .thenReturn(List.of(site));

        when(inventoryInquiryRepository.countByRequestCodeAndSiteCode("REQ011", "SITE003"))
                .thenReturn(1L);

        OrderRequestClassificationDTO result =
                siteSyncService.classifyOrderRequest("REQ011");

        assertEquals("Đã gửi hỏi tồn kho", result.getResults().get(0).getStatus());
    }
}