package itss.group11.test.uc5;

import java.util.ArrayList;
import java.util.List;

import itss.group11.entity.uc5.SiteStockDTO;
import itss.group11.entity.chung.ImportSite;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.entity.chung.OrderRequestItem;

final class AllocationServiceTestData {

    private AllocationServiceTestData() {
    }

    static Merchandise merchandise(String code) {
        return Merchandise.builder()
                .code(code)
                .name("Merchandise " + code)
                .unit("Chiáº¿c")
                .build();
    }

    static ImportSite site(String siteCode) {
        return ImportSite.builder()
                .siteCode(siteCode)
                .siteName("Site " + siteCode)
                .daysByShip(14)
                .daysByAir(3)
                .build();
    }

    static OrderRequest pendingRequest(String requestCode, OrderRequestItem... items) {
        OrderRequest request = OrderRequest.builder()
                .requestCode(requestCode)
                .status(OrderRequest.OrderRequestStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        for (OrderRequestItem item : items) {
            item.setOrderRequest(request);
            request.getItems().add(item);
        }

        return request;
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

    static OrderRequestItem item(Merchandise merchandise, int quantity) {
        return OrderRequestItem.builder()
                .merchandise(merchandise)
                .quantityOrdered(quantity)
                .build();
    }

    static SiteStockDTO stock(
            String siteCode,
            String merchandiseCode,
            int quantity,
            Integer daysByShip,
            Integer daysByAir
    ) {
        return SiteStockDTO.builder()
                .siteCode(siteCode)
                .siteName("Site " + siteCode)
                .merchandiseCode(merchandiseCode)
                .inStockQuantity(quantity)
                .daysByShip(daysByShip)
                .daysByAir(daysByAir)
                .build();
    }
}

