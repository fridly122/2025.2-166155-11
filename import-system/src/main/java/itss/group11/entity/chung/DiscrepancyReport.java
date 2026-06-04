package itss.group11.entity.chung;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "discrepancy_report")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DiscrepancyReport {

    @Id
    @Column(name = "report_id", length = 50)
    private String reportId;            // MÃ£ biÃªn báº£n

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "merchandise_code", nullable = false)
    private Merchandise merchandise;

    @Column(name = "ordered_qty", nullable = false)
    private Integer orderedQty;         // Sá»‘ lÆ°á»£ng Ä‘áº·t

    @Column(name = "received_qty", nullable = false)
    private Integer receivedQty;        // Sá»‘ lÆ°á»£ng thá»±c nháº­n (>= 0)

    @Column(name = "difference_qty", nullable = false)
    private Integer differenceQty;      // ChÃªnh lá»‡ch = orderedQty - receivedQty

    @Column(name = "reason", length = 500)
    private String reason;              // LÃ½ do sai lá»‡ch

    @Column(name = "note", length = 1000)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
