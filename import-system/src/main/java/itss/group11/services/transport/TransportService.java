package itss.group11.services.transport;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itss.group11.dto.transport.PendingPurchaseOrderDTO;
import itss.group11.dto.transport.TransportCreateDTO;
import itss.group11.dto.transport.TransportDetailDTO;
import itss.group11.dto.transport.TransportStatusUpdateDTO;
import itss.group11.dto.transport.TransportUpdateDTO;
import itss.group11.models.PurchaseOrder;
import itss.group11.models.TransportInfo;
import itss.group11.repository.orderExecution.PurchaseOrderRepository;
import itss.group11.repository.transport.TransportRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransportService {

    private static final Set<String> ALLOWED_VEHICLES = Set.of("AIR", "SHIP", "TRUCK");

    private final TransportRepository transportRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Transactional(readOnly = true)
    public List<PendingPurchaseOrderDTO> getPendingOrders() {
        return purchaseOrderRepository
                .findByStatusOrderByCreatedAtAsc(PurchaseOrder.PurchaseOrderStatus.CREATED)
                .stream()
                .map(this::toPendingOrderDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransportDetailDTO> getAllTransports() {
        return transportRepository.findAllOrderByPurchaseOrderCreatedAtAsc()
                .stream()
                .map(this::toTransportDetailDTO)
                .toList();
    }

    @Transactional
    public TransportDetailDTO createTransport(TransportCreateDTO dto) {
        validateCreateDTO(dto);
        String vehicle = normalizeVehicle(dto.getVehicle());

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByOrderId(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay don dat hang: " + dto.getOrderId()));

        if (purchaseOrder.getStatus() != PurchaseOrder.PurchaseOrderStatus.CREATED) {
            throw new RuntimeException("Chi co the tao van chuyen cho don hang o trang thai CREATED.");
        }

        if (transportRepository.existsByPurchaseOrder(purchaseOrder)) {
            throw new RuntimeException("Don hang nay da co thong tin van chuyen.");
        }

        TransportInfo transportInfo = TransportInfo.builder()
                .purchaseOrder(purchaseOrder)
                .sourceSite(purchaseOrder.getSite())
                .destinationSiteName(dto.getDestinationSiteName().trim())
                .deliveryDays(dto.getDeliveryDays())
                .vehicle(vehicle)
                .transportStatus(TransportInfo.TransportStatus.IN_TRANSIT)
                .build();

        purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.IN_TRANSIT);

        TransportInfo savedTransport = transportRepository.save(transportInfo);
        purchaseOrderRepository.save(purchaseOrder);

        return toTransportDetailDTO(savedTransport);
    }

    @Transactional
    public TransportDetailDTO updateTransportStatus(Long id, TransportStatusUpdateDTO dto) {
        if (dto == null || dto.getTransportStatus() == null || dto.getTransportStatus().isBlank()) {
            throw new RuntimeException("Trang thai van chuyen khong duoc de trong.");
        }

        TransportInfo transportInfo = transportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay thong tin van chuyen: " + id));

        TransportInfo.TransportStatus newStatus = parseTransportStatus(dto.getTransportStatus());
        transportInfo.setTransportStatus(newStatus);

        PurchaseOrder purchaseOrder = transportInfo.getPurchaseOrder();
        if (newStatus == TransportInfo.TransportStatus.CANCELLED) {
            purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.CANCELLED);
        } else {
            purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.IN_TRANSIT);
        }

        purchaseOrderRepository.save(purchaseOrder);
        return toTransportDetailDTO(transportRepository.save(transportInfo));
    }

    @Transactional
    public TransportDetailDTO updateTransport(Long id, TransportUpdateDTO dto) {
        validateUpdateDTO(dto);
        String vehicle = normalizeVehicle(dto.getVehicle());

        TransportInfo transportInfo = transportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay thong tin van chuyen: " + id));

        transportInfo.setDestinationSiteName(dto.getDestinationSiteName().trim());
        transportInfo.setDeliveryDays(dto.getDeliveryDays());
        transportInfo.setVehicle(vehicle);
        transportInfo.setTransportStatus(parseTransportStatus(dto.getTransportStatus()));

        PurchaseOrder purchaseOrder = transportInfo.getPurchaseOrder();
        if (purchaseOrder != null) {
            if (transportInfo.getTransportStatus() == TransportInfo.TransportStatus.CANCELLED) {
                purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.CANCELLED);
            } else {
                purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.IN_TRANSIT);
            }
            purchaseOrderRepository.save(purchaseOrder);
        }

        return toTransportDetailDTO(transportRepository.save(transportInfo));
    }

    private void validateCreateDTO(TransportCreateDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Du lieu tao van chuyen khong hop le.");
        }

        if (dto.getOrderId() == null || dto.getOrderId().isBlank()) {
            throw new RuntimeException("Ma don hang khong duoc de trong.");
        }

        if (dto.getDestinationSiteName() == null || dto.getDestinationSiteName().isBlank()) {
            throw new RuntimeException("Site dich khong duoc de trong.");
        }

        normalizeVehicle(dto.getVehicle());

        if (dto.getDeliveryDays() == null || dto.getDeliveryDays() <= 0 || dto.getDeliveryDays() > 365) {
            throw new RuntimeException("So ngay van chuyen phai lon hon 0 va khong qua 365.");
        }
    }

    private void validateUpdateDTO(TransportUpdateDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Du lieu cap nhat van chuyen khong hop le.");
        }

        if (dto.getDestinationSiteName() == null || dto.getDestinationSiteName().isBlank()) {
            throw new RuntimeException("Site dich khong duoc de trong.");
        }

        normalizeVehicle(dto.getVehicle());

        if (dto.getDeliveryDays() == null || dto.getDeliveryDays() <= 0 || dto.getDeliveryDays() > 365) {
            throw new RuntimeException("So ngay van chuyen phai lon hon 0 va khong qua 365.");
        }

        parseTransportStatus(dto.getTransportStatus());
    }

    private TransportInfo.TransportStatus parseTransportStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new RuntimeException("Trang thai van chuyen khong duoc de trong.");
        }

        try {
            return TransportInfo.TransportStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trang thai van chuyen khong hop le: " + status);
        }
    }

    private String normalizeVehicle(String vehicle) {
        if (vehicle == null || vehicle.isBlank()) {
            throw new RuntimeException("Phuong tien van chuyen khong duoc de trong.");
        }

        String normalizedVehicle = vehicle.trim().toUpperCase();
        if (!ALLOWED_VEHICLES.contains(normalizedVehicle)) {
            throw new RuntimeException("Phuong tien van chuyen khong hop le. Chi chap nhan AIR, SHIP hoac TRUCK.");
        }

        return normalizedVehicle;
    }

    private PendingPurchaseOrderDTO toPendingOrderDTO(PurchaseOrder purchaseOrder) {
        return PendingPurchaseOrderDTO.builder()
                .orderId(purchaseOrder.getOrderId())
                .requestCode(purchaseOrder.getOrderRequest() == null ? "" : purchaseOrder.getOrderRequest().getRequestCode())
                .siteCode(purchaseOrder.getSite() == null ? "" : purchaseOrder.getSite().getSiteCode())
                .siteName(purchaseOrder.getSite() == null ? "" : purchaseOrder.getSite().getSiteName())
                .status(purchaseOrder.getStatus().name())
                .createdDate(purchaseOrder.getCreatedAt() == null ? "" : purchaseOrder.getCreatedAt().toLocalDate().toString())
                .build();
    }

    private TransportDetailDTO toTransportDetailDTO(TransportInfo transportInfo) {
        PurchaseOrder purchaseOrder = transportInfo.getPurchaseOrder();

        return TransportDetailDTO.builder()
                .id(transportInfo.getId())
                .orderId(purchaseOrder == null ? "" : purchaseOrder.getOrderId())
                .requestCode(purchaseOrder == null || purchaseOrder.getOrderRequest() == null
                        ? ""
                        : purchaseOrder.getOrderRequest().getRequestCode())
                .sourceSiteCode(transportInfo.getSourceSite() == null ? "" : transportInfo.getSourceSite().getSiteCode())
                .sourceSiteName(transportInfo.getSourceSite() == null ? "" : transportInfo.getSourceSite().getSiteName())
                .destinationSiteName(transportInfo.getDestinationSiteName())
                .transportStatus(transportInfo.getTransportStatus().name())
                .deliveryDays(transportInfo.getDeliveryDays())
                .vehicle(transportInfo.getVehicle())
                .purchaseOrderStatus(purchaseOrder == null ? "" : purchaseOrder.getStatus().name())
                .build();
    }
}
