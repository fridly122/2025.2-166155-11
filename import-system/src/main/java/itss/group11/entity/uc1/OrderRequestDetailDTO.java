package itss.group11.entity.uc1;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDetailDTO {

    private String requestCode;
    private String status;
    private String desiredDeliveryDate;
    private String createdDate;
    private List<ItemDTO> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemDTO {
        private Long id;
        private String merchandiseCode;
        private String merchandiseName;
        private Integer quantityOrdered;
        private String unit;
    }
}

