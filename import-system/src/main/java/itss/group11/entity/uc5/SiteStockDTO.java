package itss.group11.entity.uc5;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SiteStockDTO {
    private String siteCode;
    private String siteName;
    private String merchandiseCode;
    private int inStockQuantity;
    private Integer daysByShip;
    private Integer daysByAir;

    public SiteStockDTO(String siteCode, String siteName, String merchandiseCode, int inStockQuantity) {
        this.siteCode = siteCode;
        this.siteName = siteName;
        this.merchandiseCode = merchandiseCode;
        this.inStockQuantity = inStockQuantity;
    }
}

