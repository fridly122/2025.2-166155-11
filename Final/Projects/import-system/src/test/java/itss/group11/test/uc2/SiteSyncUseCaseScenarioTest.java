package itss.group11.test.uc2;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import itss.group11.entity.chung.ImportSite;
import itss.group11.entity.chung.InventoryInquiry;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.entity.chung.OrderRequestItem;
import itss.group11.entity.uc2.InventoryInquirySendResultDTO;
import itss.group11.subsystem.chung.ImportSiteRepository;
import itss.group11.subsystem.chung.InventoryInquiryRepository;
import itss.group11.subsystem.chung.MerchandiseRepository;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.uc2.SiteSyncService;

class SiteSyncUseCaseScenarioTest {

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
    void uc2_mainScenario_classifyAndSendInventoryInquirySuccessfully() {
        Merchandise merchandise = Merchandise.builder()
                .code("MH005")
                .name("Ổ cứng SSD Samsung 1TB")
                .unit("Chiếc")
                .build();

        OrderRequestItem item = OrderRequestItem.builder()
                .id(1L)
                .merchandise(merchandise)
                .quantityOrdered(6)
                .build();

        OrderRequest request = OrderRequest.builder()
                .requestCode("REQ012")
                .status(OrderRequest.OrderRequestStatus.PENDING)
                .items(List.of(item))
                .build();

        ImportSite site = ImportSite.builder()
                .siteCode("SITE003")
                .siteName("Shanghai Warehouse")
                .build();

        InventoryInquiry savedInquiry = InventoryInquiry.builder()
                .inquiryId("INQ-TEST001")
                .orderRequest(request)
                .importSite(site)
                .status(InventoryInquiry.InquiryStatus.SENT)
                .itemCount(1)
                .build();

        when(orderRequestRepository.findByRequestCode("REQ012"))
                .thenReturn(Optional.of(request));

        when(importSiteRepository.findSitesSellingMerchandise("MH005"))
                .thenReturn(List.of(site));

        when(importSiteRepository.findBySiteCode("SITE003"))
                .thenReturn(Optional.of(site));

        when(merchandiseRepository.findByCode("MH005"))
                .thenReturn(Optional.of(merchandise));

        when(inventoryInquiryRepository.countByRequestCodeAndSiteCode("REQ012", "SITE003"))
                .thenReturn(0L);

        when(inventoryInquiryRepository.save(any(InventoryInquiry.class)))
                .thenReturn(savedInquiry);

        InventoryInquirySendResultDTO result =
                siteSyncService.sendInventoryInquiry("REQ012");

        assertEquals("REQ012", result.getRequestCode());
        assertEquals(1, result.getSiteCount());
        assertEquals(1, result.getInquiryIds().size());
        assertEquals("INQ-TEST001", result.getInquiryIds().get(0));

        verify(inventoryInquiryRepository, times(1))
                .save(any(InventoryInquiry.class));
    }

    @Test
    void uc2_alternativeScenario_inquiryAlreadySent_throwsError() {
        Merchandise merchandise = Merchandise.builder()
                .code("MH005")
                .name("Ổ cứng SSD Samsung 1TB")
                .unit("Chiếc")
                .build();

        OrderRequestItem item = OrderRequestItem.builder()
                .id(1L)
                .merchandise(merchandise)
                .quantityOrdered(6)
                .build();

        OrderRequest request = OrderRequest.builder()
                .requestCode("REQ012")
                .status(OrderRequest.OrderRequestStatus.PENDING)
                .items(List.of(item))
                .build();

        ImportSite site = ImportSite.builder()
                .siteCode("SITE003")
                .siteName("Shanghai Warehouse")
                .build();

        when(orderRequestRepository.findByRequestCode("REQ012"))
                .thenReturn(Optional.of(request));

        when(importSiteRepository.findSitesSellingMerchandise("MH005"))
                .thenReturn(List.of(site));

        when(inventoryInquiryRepository.countByRequestCodeAndSiteCode("REQ012", "SITE003"))
                .thenReturn(1L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> siteSyncService.sendInventoryInquiry("REQ012")
        );

        assertTrue(exception.getMessage().contains("đã được gửi"));

        verify(inventoryInquiryRepository, never())
                .save(any(InventoryInquiry.class));
    }
}