package itss.group11.dto.allocation;

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
}