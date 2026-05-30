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
public class ReconciliationDetailDTO {
    private String orderId;
    private String requestCode;
    private String siteCode;
    private String siteName;
    private String status;
    private List<PartialOrderSelectionDTO> lines;
}
