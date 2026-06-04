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
    private ImportSite sourceSite;      // Site nguá»“n

    @Column(name = "destination_site_name", nullable = false, length = 100)
    private String destinationSiteName; // Site Ä‘Ã­ch (kho ná»™i bá»™)

    @Column(name = "transport_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TransportStatus transportStatus;

    @Column(name = "delivery_days", nullable = false)
    private Integer deliveryDays;       // Sá»‘ ngÃ y váº­n chuyá»ƒn (pháº£i > 0 vÃ  <= 365)

    @Column(name = "vehicle", nullable = false, length = 100)
    private String vehicle;             // PhÆ°Æ¡ng tiá»‡n (Xe táº£i, TÃ u, MÃ¡y bay...)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private PurchaseOrder purchaseOrder; // ÄÆ¡n hÃ ng liÃªn quan

    public enum TransportStatus {
        IN_TRANSIT,     // Äang váº­n chuyá»ƒn
        COMPLETED,      // HoÃ n thÃ nh
        CANCELLED       // ÄÃ£ há»§y
    }
}
