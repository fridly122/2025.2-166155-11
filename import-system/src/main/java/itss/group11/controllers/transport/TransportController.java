package itss.group11.controllers.transport;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import itss.group11.dto.transport.TransportCreateDTO;
import itss.group11.dto.transport.TransportStatusUpdateDTO;
import itss.group11.dto.transport.TransportUpdateDTO;
import itss.group11.services.transport.TransportService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transports")
@RequiredArgsConstructor
public class TransportController {

    private final TransportService transportService;

    @GetMapping("/pending-orders")
    public ResponseEntity<?> getPendingOrders() {
        try {
            return ResponseEntity.ok(transportService.getPendingOrders());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("Khong the tai danh sach don hang cho van chuyen: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTransports() {
        try {
            return ResponseEntity.ok(transportService.getAllTransports());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("Khong the tai danh sach van chuyen: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createTransport(@RequestBody TransportCreateDTO dto) {
        try {
            return ResponseEntity.ok(transportService.createTransport(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Tao van chuyen that bai: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransport(
            @PathVariable("id") Long id,
            @RequestBody TransportUpdateDTO dto
    ) {
        try {
            return ResponseEntity.ok(transportService.updateTransport(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Cap nhat thong tin van chuyen that bai: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateTransportStatus(
            @PathVariable("id") Long id,
            @RequestBody TransportStatusUpdateDTO dto
    ) {
        try {
            return ResponseEntity.ok(transportService.updateTransportStatus(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Cap nhat trang thai van chuyen that bai: " + e.getMessage());
        }
    }
}
