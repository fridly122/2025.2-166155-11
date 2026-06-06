package itss.group11.test.uc4;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import itss.group11.subsystem.chung.PurchaseOrderRepository;
import itss.group11.subsystem.chung.TransportRepository;
import itss.group11.subsystem.uc4.TransportService;

@ExtendWith(MockitoExtension.class)
class TransportServiceWhiteBoxTest {

    @Mock
    private TransportRepository transportRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @InjectMocks
    private TransportService transportService;

    @Test
    void branch_vehicleNull() {

        assertThrows(
                RuntimeException.class,
                () -> transportService.createTransport(
                        TransportServiceTestData.createDto(
                                "PO001",
                                "HCM",
                                3,
                                null
                        )
                )
        );
    }

    @Test
    void branch_deliveryDaysGreaterThan365() {

        assertThrows(
                RuntimeException.class,
                () -> transportService.createTransport(
                        TransportServiceTestData.createDto(
                                "PO001",
                                "HCM",
                                366,
                                "AIR"
                        )
                )
        );
    }

}