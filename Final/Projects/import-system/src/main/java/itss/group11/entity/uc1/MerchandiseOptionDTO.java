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
public class MerchandiseOptionDTO {

    private String code;
    private String name;
    private String unit;
}

