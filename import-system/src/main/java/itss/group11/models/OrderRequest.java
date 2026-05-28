package itss.group11.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "order_request")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OrderRequest {

    @Id
    @Column(name = "request_code", length = 50)
    private String requestCode;         // Mã yêu cầu (VD: "YC-BH-002")

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private OrderRequestStatus status;  // Trạng thái

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;           // Nhân viên bán hàng tạo yêu cầu

    // Danh sách mặt hàng trong yêu cầu này
    @OneToMany(mappedBy = "orderRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderRequestItem> items;

    public enum OrderRequestStatus {
        PENDING,        // Chờ xử lý
        PROCESSING,     // Đang xử lý
        ORDERED,        // Đã đặt hàng
        COMPLETED,      // Hoàn thành
        CANCELLED       // Đã hủy
    }
}