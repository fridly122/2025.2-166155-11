package itss.group11.controller.uc6;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import itss.group11.entity.uc6.ReconciliationSubmitDTO;
import itss.group11.subsystem.uc6.PurchaseOrderService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping("/in-transit")
    public ResponseEntity<?> getInTransitOrders() {
        try {
            return ResponseEntity.ok(purchaseOrderService.getInTransitOrders());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("Khong the tai danh sach don hang dang van chuyen: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}/reconciliation")
    public ResponseEntity<?> getReconciliationDetail(@PathVariable("orderId") String orderId) {
        try {
            return ResponseEntity.ok(purchaseOrderService.getReconciliationDetail(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Khong the tai chi tiet doi soat: " + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/reconcile")
    public ResponseEntity<?> reconcile(
            @PathVariable("orderId") String orderId,
            @RequestBody ReconciliationSubmitDTO dto
    ) {
        try {
            return ResponseEntity.ok(purchaseOrderService.reconcile(orderId, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Doi soat that bai: " + e.getMessage());
        }
    }
}

