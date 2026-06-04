package itss.group11.entity.uc5;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO chứa kết quả trả về sau khi thực hiện xử lý lập kế hoạch đặt hàng (UC005)
 * Dùng để đẩy dữ liệu từ Service qua Controller và trả về cho Frontend hiển thị.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationResultDTO {
    
    private String requestCode;       // Mã yêu cầu nhập hàng (VD: REQ-9942A)
    private boolean isSuccess;        // Trạng thái xử lý (true: Thành công, false: Thất bại)
    private String message;           // Thông báo chi tiết (VD: "Phân bổ thành công", "Thiếu hàng tồn kho")
    private List<String> generatedPoCodes; // Danh sách các mã đơn hàng PO được tự động sinh ra sau khi phân bổ
}
