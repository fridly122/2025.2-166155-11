package itss.group11.dto.orderExecution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartialOrderSelectionDTO {
    private Long lineId;
    private String merchandiseCode;
    private String merchandiseName;
    private Integer orderedQty;
    private Integer receivedQty;
    private Integer differenceQty;
    private String unit;
}
