package itss.group11.entity.uc2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryInquiryRequestDTO {

    private String merchandiseCode;
    private Integer requiredQuantity;
}

