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
    private String orderId;             // Mã đơn (VD: "DH001")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_code")
    private OrderRequest orderRequest;  // Yêu cầu gốc từ BP bán hàng

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_code")
    private ImportSite site;            // Site được chọn

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderLine> orderLines;
    public enum PurchaseOrderStatus {
        CREATED,            // Vừa tạo
        CONFIRMED,          // Site đã xác nhận
        IN_TRANSIT,         // Đang vận chuyển
        RECEIVED,           // Đã nhập kho
        CANCELLED           // Đã hủy
    }
}
