package itss.group11.dto.siteSync;

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
public class InventoryInquiryResponseDTO {

    private String merchandiseCode;
    private String merchandiseName;
    private String unit;
    private Integer requiredQuantity;
    private Integer totalStock;
    private boolean enoughInventory;
    private String message;
    private List<SiteClassificationResultDTO> results;
}
