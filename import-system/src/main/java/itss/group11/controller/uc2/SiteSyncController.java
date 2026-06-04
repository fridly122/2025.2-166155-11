package itss.group11.controller.uc2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import itss.group11.entity.uc2.InventoryInquiryRequestDTO;
import itss.group11.subsystem.uc2.SiteSyncService;
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
                    .body("KhÃ´ng thá»ƒ táº£i danh sÃ¡ch yÃªu cáº§u nháº­p hÃ ng: " + e.getMessage());
        }
    }

    @GetMapping("/requests/{requestCode}")
    public ResponseEntity<?> getRequestDetail(@PathVariable("requestCode") String requestCode) {
        try {
            return ResponseEntity.ok(siteSyncService.getRequestDetail(requestCode));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("KhÃ´ng thá»ƒ táº£i chi tiáº¿t yÃªu cáº§u nháº­p hÃ ng: " + e.getMessage());
        }
    }

    @PostMapping("/requests/{requestCode}/classify")
    public ResponseEntity<?> classifyOrderRequest(@PathVariable("requestCode") String requestCode) {
        try {
            return ResponseEntity.ok(siteSyncService.classifyOrderRequest(requestCode));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("PhÃ¢n loáº¡i yÃªu cáº§u nháº­p hÃ ng tháº¥t báº¡i: " + e.getMessage());
        }
    }

    @PostMapping("/requests/{requestCode}/send-inventory-inquiry")
    public ResponseEntity<?> sendInventoryInquiry(@PathVariable("requestCode") String requestCode) {
        try {
            return ResponseEntity.ok(siteSyncService.sendInventoryInquiry(requestCode));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Gá»­i yÃªu cáº§u há»i tá»“n kho tháº¥t báº¡i: " + e.getMessage());
        }
    }

    @GetMapping("/merchandise")
    public ResponseEntity<?> getMerchandiseOptions() {
        try {
            return ResponseEntity.ok(siteSyncService.getMerchandiseOptions());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("KhÃ´ng thá»ƒ táº£i danh sÃ¡ch máº·t hÃ ng: " + e.getMessage());
        }
    }

    @PostMapping("/inventory-inquiry")
    public ResponseEntity<?> classifySites(@RequestBody InventoryInquiryRequestDTO requestDTO) {
        try {
            return ResponseEntity.ok(siteSyncService.classifySites(requestDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("TÃ¬m site vÃ  phÃ¢n loáº¡i tháº¥t báº¡i: " + e.getMessage());
        }
    }
}

