package itss.group11.controllers.allocation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import itss.group11.dto.allocation.AllocationPlanDTO;
import itss.group11.dto.allocation.AllocationResultDTO;
import itss.group11.services.allocation.AllocationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/allocations")
@RequiredArgsConstructor
public class AllocationController {

    private final AllocationService allocationService;

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests() {
        try {
            return ResponseEntity.ok(allocationService.getPendingRequests());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("Không thể tải danh sách yêu cầu chờ phân bổ: " + e.getMessage());
        }
    }

    @GetMapping("/preview/{requestCode}")
    public ResponseEntity<?> previewAllocationPlan(@PathVariable("requestCode") String requestCode) {
        try {
            AllocationPlanDTO result = allocationService.previewAllocationPlan(requestCode);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Không thể xem trước kế hoạch: " + e.getMessage());
        }
    }

    @PostMapping("/process/{requestCode}")
    public ResponseEntity<?> processAllocationPlan(@PathVariable("requestCode") String requestCode) {
        try {
            AllocationResultDTO result = allocationService.processAllocationPlan(requestCode);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if ("INSUFFICIENT_INVENTORY".equals(e.getMessage())) {
                return ResponseEntity.badRequest()
                        .body("Lỗi: Tổng tồn kho từ các Site không đủ để đáp ứng yêu cầu nhập hàng.");
            }

            return ResponseEntity.badRequest()
                    .body("Xử lý thất bại: " + e.getMessage());
        }
    }
}