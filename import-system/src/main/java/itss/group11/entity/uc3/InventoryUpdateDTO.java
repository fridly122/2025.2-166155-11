package itss.group11.entity.uc3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateDTO {

    private String siteCode;
    private String merchandiseCode;
    private Integer inStockQuantity;
}

