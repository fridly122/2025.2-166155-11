package itss.group11.entity.uc6;

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

