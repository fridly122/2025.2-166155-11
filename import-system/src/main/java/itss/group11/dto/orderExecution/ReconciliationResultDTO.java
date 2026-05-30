package itss.group11.dto.orderExecution;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationResultDTO {
    private String orderId;
    private String status;
    private boolean hasDiscrepancy;
    private String message;
    private List<String> discrepancyReportIds;
}
