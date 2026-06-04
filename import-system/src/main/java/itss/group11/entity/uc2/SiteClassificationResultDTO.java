package itss.group11.entity.uc2;

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
public class SiteClassificationResultDTO {

    private String siteCode;
    private String siteName;
    private String merchandiseCode;
    private String merchandiseName;
    private String unit;
    private Integer requiredQuantity;
    private Integer inStockQuantity;
    private String classification;
    private String suggestedTransportMeans;
    private Integer estimatedDeliveryDays;
    private Integer daysByAir;
    private Integer daysByShip;
    private String otherInfo;
}

