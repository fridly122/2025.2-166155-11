package itss.group11.test.uc4;

import itss.group11.entity.uc4.TransportCreateDTO;
import itss.group11.entity.uc4.TransportUpdateDTO;

final class TransportServiceTestData {

    private TransportServiceTestData() {
    }

    static TransportCreateDTO createDto(
            String orderId,
            String destination,
            Integer days,
            String vehicle
    ) {
        return new TransportCreateDTO(
                orderId,
                destination,
                days,
                vehicle
        );
    }

    static TransportUpdateDTO updateDto(
            String destination,
            Integer days,
            String vehicle,
            String status
    ) {
        return new TransportUpdateDTO(
                destination,
                days,
                vehicle,
                status
        );
    }

}