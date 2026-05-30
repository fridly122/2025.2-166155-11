package itss.group11.controllers.siteSync;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import itss.group11.dto.siteSync.InventoryInquiryRequestDTO;
import itss.group11.services.siteSync.SiteSyncService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/site-sync")
@RequiredArgsConstructor
public class SiteSyncController {

    private final SiteSyncService siteSyncService;

    @GetMapping("/pending-requests")
    public ResponseEntity<?> getPendingRequests() {
        try {
            return ResponseEntity.ok(siteSyncService.getPendingRequests());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("Không thể tải danh sách yêu cầu nhập hàng: " + e.getMessage());
        }
    }

    @GetMapping("/requests/{requestCode}")
    public ResponseEntity<?> getRequestDetail(@PathVariable("requestCode") String requestCode) {
        try {
            return ResponseEntity.ok(siteSyncService.getRequestDetail(requestCode));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Không thể tải chi tiết yêu cầu nhập hàng: " + e.getMessage());
        }
    }

    @PostMapping("/requests/{requestCode}/classify")
    public ResponseEntity<?> classifyOrderRequest(@PathVariable("requestCode") String requestCode) {
        try {
            return ResponseEntity.ok(siteSyncService.classifyOrderRequest(requestCode));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Phân loại yêu cầu nhập hàng thất bại: " + e.getMessage());
        }
    }

    @PostMapping("/requests/{requestCode}/send-inventory-inquiry")
    public ResponseEntity<?> sendInventoryInquiry(@PathVariable("requestCode") String requestCode) {
        try {
            return ResponseEntity.ok(siteSyncService.sendInventoryInquiry(requestCode));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Gửi yêu cầu hỏi tồn kho thất bại: " + e.getMessage());
        }
    }

    @GetMapping("/merchandise")
    public ResponseEntity<?> getMerchandiseOptions() {
        try {
            return ResponseEntity.ok(siteSyncService.getMerchandiseOptions());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("Không thể tải danh sách mặt hàng: " + e.getMessage());
        }
    }

    @PostMapping("/inventory-inquiry")
    public ResponseEntity<?> classifySites(@RequestBody InventoryInquiryRequestDTO requestDTO) {
        try {
            return ResponseEntity.ok(siteSyncService.classifySites(requestDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Tìm site và phân loại thất bại: " + e.getMessage());
        }
    }
}
