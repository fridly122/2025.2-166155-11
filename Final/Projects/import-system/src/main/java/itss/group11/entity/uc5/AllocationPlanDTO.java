package itss.group11.entity.uc5;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationPlanDTO {
    private String requestCode;
    private boolean isEnoughInventory; // true: Đủ hàng, false: Bị thiếu hàng
    private String message;
    private List<AllocationPlanItemDTO> planItems;
}
