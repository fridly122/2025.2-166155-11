package itss.group11.entity.chung;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transport_info")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TransportInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_site_code", nullable = false)
    private ImportSite sourceSite;      // Site nguồn

    @Column(name = "destination_site_name", nullable = false, length = 100)
    private String destinationSiteName; // Site đích (kho nội bộ)

    @Column(name = "transport_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TransportStatus transportStatus;

    @Column(name = "delivery_days", nullable = false)
    private Integer deliveryDays;       // Số ngày vận chuyển (phải > 0 và <= 365)

    @Column(name = "vehicle", nullable = false, length = 100)
    private String vehicle;             // Phương tiện (Xe tải, Tàu, Máy bay...)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private PurchaseOrder purchaseOrder; // Đơn hàng liên quan

    public enum TransportStatus {
        IN_TRANSIT,     // Đang vận chuyển
        COMPLETED,      // Hoàn thành
        CANCELLED       // Đã hủy
    }
}
