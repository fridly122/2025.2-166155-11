package itss.group11.entity.uc5;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationInventorySummaryDTO {
    private String merchandiseCode;
    private String merchandiseName;
    private int requestedQuantity;
    private int totalInStockQuantity;
    private int shortageQuantity;
}
