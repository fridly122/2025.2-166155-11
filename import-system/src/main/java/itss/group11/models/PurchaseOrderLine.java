package itss.group11.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "purchase_order_line")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PurchaseOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "merchandise_code", nullable = false)
    private Merchandise merchandise;

    @Column(name = "ordered_qty", nullable = false)
    private Integer orderedQty;         // Số lượng đặt

    @Column(name = "received_qty")
    private Integer receivedQty;        // Số lượng thực nhận (sau khi kho nhập)

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "delivery_means", length = 20)
    @Enumerated(EnumType.STRING)
    private DeliveryMeans deliveryMeans; // Phương thức vận chuyển

    public enum DeliveryMeans {
        SHIP,   // Tàu
        AIR     // Hàng không
    }

    // Tính chênh lệch số lượng (dùng cho đối soát - UC006)
    public int calculateDifference() {
        if (receivedQty == null) return orderedQty;
        return orderedQty - receivedQty;
    }
}