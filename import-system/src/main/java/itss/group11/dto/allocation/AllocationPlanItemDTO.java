package itss.group11.dto.allocation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationPlanItemDTO {
    private String merchandiseCode;
    private String siteCode;
    private String siteName;
    private int allocatedQuantity;
}