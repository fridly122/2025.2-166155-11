package itss.group11.entity.uc4;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransportUpdateDTO {
    private String destinationSiteName;
    private Integer deliveryDays;
    private String vehicle;
    private String transportStatus;
}

