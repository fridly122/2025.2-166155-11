package itss.group11.controller.uc1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import itss.group11.entity.uc1.OrderRequestCreationDTO;
import itss.group11.subsystem.uc1.OrderRequestService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/order-requests")
@RequiredArgsConstructor
public class OrderRequestController {

    private final OrderRequestService orderRequestService;

    @GetMapping
    public ResponseEntity<?> getAllRequests(@RequestParam(value = "requestCode", required = false) String requestCode) {
        try {
            if (requestCode != null && !requestCode.isBlank()) {
                return ResponseEntity.ok(orderRequestService.searchRequests(requestCode));
            }
            return ResponseEntity.ok(orderRequestService.getAllRequests());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("Không thể tải danh sách yêu cầu nhập hàng: " + e.getMessage());
        }
    }

    @GetMapping("/{requestCode}")
    public ResponseEntity<?> getRequestDetail(@PathVariable("requestCode") String requestCode) {
        try {
            return ResponseEntity.ok(orderRequestService.getRequestDetail(requestCode));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Không thể tải chi tiết yêu cầu nhập hàng: " + e.getMessage());
        }
    }

    @GetMapping("/merchandise")
    public ResponseEntity<?> getMerchandiseOptions() {
        try {
            return ResponseEntity.ok(orderRequestService.getMerchandiseOptions());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("Không thể tải danh sách mặt hàng: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody OrderRequestCreationDTO dto) {
        try {
            return ResponseEntity.ok(orderRequestService.createRequest(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Tạo yêu cầu nhập hàng thất bại: " + e.getMessage());
        }
    }

    @PutMapping("/{requestCode}")
    public ResponseEntity<?> updateRequest(
            @PathVariable("requestCode") String requestCode,
            @RequestBody OrderRequestCreationDTO dto
    ) {
        try {
            return ResponseEntity.ok(orderRequestService.updateRequest(requestCode, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Cập nhật yêu cầu nhập hàng thất bại: " + e.getMessage());
        }
    }

    @DeleteMapping("/{requestCode}")
    public ResponseEntity<?> deleteRequest(@PathVariable("requestCode") String requestCode) {
        try {
            return ResponseEntity.ok(orderRequestService.deleteRequest(requestCode));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Hủy/xóa yêu cầu nhập hàng thất bại: " + e.getMessage());
        }
    }
}

