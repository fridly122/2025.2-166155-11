package itss.group11.entity.uc1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestSummaryDTO {

    private String requestCode;
    private String status;
    private String desiredDeliveryDate;
    private String createdDate;
    private Integer itemCount;
}

