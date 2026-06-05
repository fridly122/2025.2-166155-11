package itss.group11.entity.uc4;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportDetailDTO {
    private Long id;
    private String orderId;
    private String requestCode;
    private String sourceSiteCode;
    private String sourceSiteName;
    private String destinationSiteName;
    private String transportStatus;
    private Integer deliveryDays;
    private String vehicle;
    private String purchaseOrderStatus;
}
