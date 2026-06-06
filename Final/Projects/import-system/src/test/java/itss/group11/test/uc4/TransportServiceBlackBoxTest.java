package itss.group11.test.uc4;

import static itss.group11.test.uc4.TransportServiceTestData.createDto;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import itss.group11.entity.chung.PurchaseOrder;
import itss.group11.subsystem.chung.PurchaseOrderRepository;
import itss.group11.subsystem.chung.TransportRepository;
import itss.group11.subsystem.uc4.TransportService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportServiceBlackBoxTest {

    @Mock
    private TransportRepository transportRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @InjectMocks
    private TransportService transportService;

    @Test
    void createTransport_nullDto_throwsError() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transportService.createTransport(null)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Du lieu tao van chuyen")
        );
    }

    @Test
    void createTransport_emptyOrderId_throwsError() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transportService.createTransport(
                                createDto(
                                        "",
                                        "HCM",
                                        3,
                                        "AIR"
                                )
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("Ma don hang")
        );
    }

    @Test
    void createTransport_emptyDestination_throwsError() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transportService.createTransport(
                                createDto(
                                        "PO001",
                                        "",
                                        3,
                                        "AIR"
                                )
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("Site dich")
        );
    }

    @Test
    void createTransport_invalidVehicle_throwsError() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transportService.createTransport(
                                createDto(
                                        "PO001",
                                        "HCM",
                                        3,
                                        "BIKE"
                                )
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("Phuong tien van chuyen khong hop le")
        );
    }

    @Test
    void createTransport_invalidDeliveryDays_throwsError() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transportService.createTransport(
                                createDto(
                                        "PO001",
                                        "HCM",
                                        0,
                                        "AIR"
                                )
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("So ngay van chuyen")
        );
    }

    @Test
    void createTransport_orderNotFound_throwsError() {

        when(
                purchaseOrderRepository.findByOrderId("PO999")
        ).thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transportService.createTransport(
                                createDto(
                                        "PO999",
                                        "HCM",
                                        3,
                                        "AIR"
                                )
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("Khong tim thay don dat hang")
        );
    }

    @Test
    void createTransport_orderStatusNotCreated_throwsError() {

        PurchaseOrder po = new PurchaseOrder();

        po.setOrderId("PO001");
        po.setStatus(
                PurchaseOrder.PurchaseOrderStatus.IN_TRANSIT
        );

        when(
                purchaseOrderRepository.findByOrderId("PO001")
        ).thenReturn(Optional.of(po));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> transportService.createTransport(
                                createDto(
                                        "PO001",
                                        "HCM",
                                        3,
                                        "AIR"
                                )
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("Chi co the tao van chuyen")
        );
    }

}