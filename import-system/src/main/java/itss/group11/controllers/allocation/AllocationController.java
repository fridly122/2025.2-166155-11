package itss.group11.controllers.allocation;

import itss.group11.dto.allocation.AllocationResultDTO;
import itss.group11.services.allocation.AllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/allocations")
@RequiredArgsConstructor
public class AllocationController {

    // Nhờ @RequiredArgsConstructor của Lombok, Spring sẽ tự động inject AllocationService vào đây
    private final AllocationService allocationService;

    /**
     * API thực thi luồng Xử lý lập kế hoạch đặt hàng (UC005)
     * Phương thức: POST
     * Đường dẫn: /api/v1/allocations/process/{requestCode}
     */
    @PostMapping("/process/{requestCode}")
    public ResponseEntity<?> processAllocationPlan(@PathVariable String requestCode) {
        try {
            // Gọi Service để chạy thuật toán
            AllocationResultDTO result = allocationService.processAllocationPlan(requestCode);
            
            // Trả về HTTP 200 OK kèm dữ liệu DTO nếu thành công
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            // Xử lý các kịch bản ngoại lệ do Service ném ra
            if ("INSUFFICIENT_INVENTORY".equals(e.getMessage())) {
                // Trả về HTTP 400 Bad Request kèm thông báo để Frontend hiển thị Pop-up
                // "CONTACT SALES FOR INVENTORY INQUIRIES" hoặc nút "PROCEED WITH PARTIAL ORDER"
                return ResponseEntity.badRequest()
                        .body("Lỗi: Tổng tồn kho từ các Site không đủ để đáp ứng yêu cầu nhập hàng.");
            }
            
            // Các lỗi hệ thống khác trả về HTTP 500
            return ResponseEntity.internalServerError()
                    .body("Đã xảy ra lỗi trong quá trình phân bổ: " + e.getMessage());
        }
    }

    /**
     * Tùy chọn thêm: API Preview (Xem trước kế hoạch)
     * Dùng để Frontend gọi và hiển thị ra bảng gợi ý phân bổ trước khi người dùng thực sự bấm nút CHỐT (Process)
     */
    @GetMapping("/preview/{requestCode}")
    public ResponseEntity<?> previewAllocationPlan(@PathVariable String requestCode) {
        // Tạm thời để trống, bạn có thể tạo thêm hàm previewAllocationPlan() trong Service 
        // trả về AllocationPlanDTO nếu hệ thống yêu cầu nhân viên phải xem và duyệt trước.
        return ResponseEntity.ok("Tính năng xem trước kế hoạch đang được xây dựng.");
    }
}