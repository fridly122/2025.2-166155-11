package itss.group11.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "order_request_item")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OrderRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_code", nullable = false)
    private OrderRequest orderRequest;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "merchandise_code", nullable = false)
    private Merchandise merchandise;

    @Column(name = "quantity_ordered", nullable = false)
    private Integer quantityOrdered;    // Số lượng cần đặt

    @Column(name = "unit", length = 50)
    private String unit;                // Đơn vị

    @Column(name = "desired_delivery_date", nullable = false)
    private LocalDate desiredDeliveryDate;  // Ngày nhận mong muốn
}