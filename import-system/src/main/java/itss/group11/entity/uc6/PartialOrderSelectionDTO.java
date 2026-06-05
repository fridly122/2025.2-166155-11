package itss.group11.entity.uc6;

import java.math.BigDecimal;

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
    private BigDecimal unitPrice;
    private String unit;
}
