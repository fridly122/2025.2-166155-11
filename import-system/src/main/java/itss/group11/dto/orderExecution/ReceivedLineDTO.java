package itss.group11.dto.orderExecution;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceivedLineDTO {
    private Long lineId;
    private Integer receivedQty;
}
