package itss.group11.entity.uc5;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationRequestRowDTO {
    private String requestCode;
    private String status;
    private String createdDate;
}
