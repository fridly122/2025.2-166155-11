package itss.group11.dto.warehouse;

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
public class InternalInventoryRowDTO {

    private Long id;
    private String merchandiseCode;
    private String merchandiseName;
    private String unit;
    private Integer inStockQuantity;
    private String stockStatus;
    private String updatedAt;
}
