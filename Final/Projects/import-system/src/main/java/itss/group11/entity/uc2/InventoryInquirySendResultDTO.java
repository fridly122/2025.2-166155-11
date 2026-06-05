package itss.group11.entity.uc2;

import java.util.List;

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
public class InventoryInquirySendResultDTO {

    private String requestCode;
    private Integer siteCount;
    private Integer itemCount;
    private List<String> inquiryIds;
    private String message;
}

