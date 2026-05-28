package itss.group11.models;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "desired_delivery_date")
    private LocalDate desiredDeliveryDate; // Ngày nhận mong muốn từ BP Bán Hàng (Bổ sung)

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