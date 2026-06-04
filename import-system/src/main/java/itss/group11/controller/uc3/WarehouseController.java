package itss.group11.controller.uc3;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import itss.group11.entity.uc3.InventoryUpdateDTO;
import itss.group11.subsystem.uc3.InventoryManagementService;
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
                    .body("KhÃ´ng thá»ƒ táº£i danh sÃ¡ch tá»“n kho: " + e.getMessage());
        }
    }

    @GetMapping("/internal-inventory")
    public ResponseEntity<?> getInternalInventoryRows() {
        try {
            return ResponseEntity.ok(inventoryManagementService.getInternalInventoryRows());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("KhÃ´ng thá»ƒ táº£i danh sÃ¡ch tá»“n kho ná»™i bá»™: " + e.getMessage());
        }
    }

    @GetMapping("/sites")
    public ResponseEntity<?> getSiteOptions() {
        try {
            return ResponseEntity.ok(inventoryManagementService.getSiteOptions());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("KhÃ´ng thá»ƒ táº£i danh sÃ¡ch site: " + e.getMessage());
        }
    }

    @GetMapping("/merchandise")
    public ResponseEntity<?> getMerchandiseOptions() {
        try {
            return ResponseEntity.ok(inventoryManagementService.getMerchandiseOptions());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("KhÃ´ng thá»ƒ táº£i danh sÃ¡ch máº·t hÃ ng: " + e.getMessage());
        }
    }

    @PostMapping("/inventory")
    public ResponseEntity<?> updateInventory(@RequestBody InventoryUpdateDTO dto) {
        try {
            return ResponseEntity.ok(inventoryManagementService.updateInventory(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Cáº­p nháº­t tá»“n kho tháº¥t báº¡i: " + e.getMessage());
        }
    }
}

