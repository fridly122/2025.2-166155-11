package itss.group11.test.uc1;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import itss.group11.entity.uc1.OrderRequestCreationDTO;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.entity.chung.OrderRequestItem;

final class OrderRequestServiceTestData {

    private OrderRequestServiceTestData() {
    }

    static String futureDate(int plusDays) {
        return LocalDate.now().plusDays(plusDays).toString();
    }

    static Merchandise merchandise(String code) {
        return Merchandise.builder()
                .code(code)
                .name("Merchandise " + code)
                .unit("Cai")
                .build();
    }

    static OrderRequestCreationDTO creationDto(
            String requestCode,
            String desiredDeliveryDate,
            OrderRequestCreationDTO.ItemDTO... items
    ) {
        OrderRequestCreationDTO dto = new OrderRequestCreationDTO();
        dto.setRequestCode(requestCode);
        dto.setDesiredDeliveryDate(desiredDeliveryDate);
        dto.setItems(Arrays.asList(items));
        return dto;
    }

    static OrderRequestCreationDTO.ItemDTO line(String merchandiseCode, Integer quantity) {
        return new OrderRequestCreationDTO.ItemDTO(merchandiseCode, quantity);
    }

    static OrderRequest pendingRequest(String requestCode, OrderRequestItem... items) {
        OrderRequest request = OrderRequest.builder()
                .requestCode(requestCode)
                .status(OrderRequest.OrderRequestStatus.PENDING)
                .desiredDeliveryDate(LocalDate.now().plusDays(7))
                .items(new java.util.ArrayList<>())
                .build();

        for (OrderRequestItem item : items) {
            item.setOrderRequest(request);
            request.getItems().add(item);
        }

        return request;
    }

    static OrderRequestItem item(Merchandise merchandise, int quantity) {
        return OrderRequestItem.builder()
                .merchandise(merchandise)
                .quantityOrdered(quantity)
                .build();
    }

    static OrderRequest requestWithStatus(
            String requestCode,
            OrderRequest.OrderRequestStatus status,
            List<OrderRequestItem> items
    ) {
        return OrderRequest.builder()
                .requestCode(requestCode)
                .status(status)
                .items(items)
                .build();
    }
}
