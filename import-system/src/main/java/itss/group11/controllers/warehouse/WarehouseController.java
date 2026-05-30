package itss.group11.controllers.warehouse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import itss.group11.dto.warehouse.InventoryUpdateDTO;
import itss.group11.services.warehouse.InventoryManagementService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final InventoryManagementService inventoryManagementService;

    @GetMapping("/inventory")
    public ResponseEntity<?> getInventoryRows() {
        try {
            return ResponseEntity.ok(inventoryManagementService.getInventoryRows());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("Không thể tải danh sách tồn kho: " + e.getMessage());
        }
    }

    @GetMapping("/internal-inventory")
    public ResponseEntity<?> getInternalInventoryRows() {
        try {
            return ResponseEntity.ok(inventoryManagementService.getInternalInventoryRows());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("Không thể tải danh sách tồn kho nội bộ: " + e.getMessage());
        }
    }

    @GetMapping("/sites")
    public ResponseEntity<?> getSiteOptions() {
        try {
            return ResponseEntity.ok(inventoryManagementService.getSiteOptions());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("Không thể tải danh sách site: " + e.getMessage());
        }
    }

    @GetMapping("/merchandise")
    public ResponseEntity<?> getMerchandiseOptions() {
        try {
            return ResponseEntity.ok(inventoryManagementService.getMerchandiseOptions());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("Không thể tải danh sách mặt hàng: " + e.getMessage());
        }
    }

    @PostMapping("/inventory")
    public ResponseEntity<?> updateInventory(@RequestBody InventoryUpdateDTO dto) {
        try {
            return ResponseEntity.ok(inventoryManagementService.updateInventory(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Cập nhật tồn kho thất bại: " + e.getMessage());
        }
    }
}
