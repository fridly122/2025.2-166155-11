package itss.group11.controller.uc5;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import itss.group11.entity.uc5.AllocationPlanDTO;
import itss.group11.entity.uc5.AllocationResultDTO;
import itss.group11.subsystem.uc5.AllocationService;
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
                    .body("KhÃ´ng thá»ƒ táº£i danh sÃ¡ch yÃªu cáº§u chá» phÃ¢n bá»•: " + e.getMessage());
        }
    }

    @GetMapping("/preview/{requestCode}")
    public ResponseEntity<?> previewAllocationPlan(@PathVariable("requestCode") String requestCode) {
        try {
            AllocationPlanDTO result = allocationService.previewAllocationPlan(requestCode);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("KhÃ´ng thá»ƒ xem trÆ°á»›c káº¿ hoáº¡ch: " + e.getMessage());
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
                        .body("Lá»—i: Tá»•ng tá»“n kho tá»« cÃ¡c Site khÃ´ng Ä‘á»§ Ä‘á»ƒ Ä‘Ã¡p á»©ng yÃªu cáº§u nháº­p hÃ ng.");
            }

            return ResponseEntity.badRequest()
                    .body("Xá»­ lÃ½ tháº¥t báº¡i: " + e.getMessage());
        }
    }
}
