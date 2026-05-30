package itss.group11.dto.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingPurchaseOrderDTO {
    private String orderId;
    private String requestCode;
    private String siteCode;
    private String siteName;
    private String status;
    private String createdDate;
}