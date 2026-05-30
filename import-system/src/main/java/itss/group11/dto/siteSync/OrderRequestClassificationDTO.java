package itss.group11.dto.siteSync;

import java.util.List;

import itss.group11.dto.requestManage.OrderRequestDetailDTO;
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
public class OrderRequestClassificationDTO {

    private String requestCode;
    private String status;
    private String desiredDeliveryDate;
    private Integer itemCount;
    private Integer siteCount;
    private String message;
    private List<OrderRequestDetailDTO.ItemDTO> items;
    private List<SiteClassificationResultDTO> results;
}
