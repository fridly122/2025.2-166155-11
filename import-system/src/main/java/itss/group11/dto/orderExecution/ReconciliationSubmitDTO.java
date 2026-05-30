package itss.group11.dto.orderExecution;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationSubmitDTO {
    private List<ReceivedLineDTO> lines;
    private String reason;
    private String note;
    private String createdBy;
}
