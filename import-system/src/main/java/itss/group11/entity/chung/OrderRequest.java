package itss.group11.entity.chung;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @Id
    @Column(name = "request_code", length = 50)
    private String requestCode;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private OrderRequestStatus status;

    @Column(name = "desired_delivery_date")
    private LocalDate desiredDeliveryDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private String createdBy;

    @OneToMany(mappedBy = "orderRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderRequestItem> items;

    public enum OrderRequestStatus {
        PENDING,
        PROCESSING,
        ORDERED,
        COMPLETED,
        CANCELLED
    }
}
