package itss.group11.entity.chung;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "purchase_order")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PurchaseOrder {

    @Id
    @Column(name = "order_id", length = 50)
    private String orderId;             // MÃ£ Ä‘Æ¡n (VD: "DH001")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_code")
    private OrderRequest orderRequest;  // YÃªu cáº§u gá»‘c tá»« BP bÃ¡n hÃ ng

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_code")
    private ImportSite site;            // Site Ä‘Æ°á»£c chá»n

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderLine> orderLines;
    public enum PurchaseOrderStatus {
        CREATED,            // Vá»«a táº¡o
        CONFIRMED,          // Site Ä‘Ã£ xÃ¡c nháº­n
        IN_TRANSIT,         // Äang váº­n chuyá»ƒn
        RECEIVED,           // ÄÃ£ nháº­p kho
        CANCELLED           // ÄÃ£ há»§y
    }
}
