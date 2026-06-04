package itss.group11.entity.chung;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_inquiry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryInquiry {

    @Id
    @Column(name = "inquiry_id", length = 50)
    private String inquiryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_code", nullable = false)
    private OrderRequest orderRequest;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_code", nullable = false)
    private ImportSite importSite;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private InquiryStatus status;

    @Column(name = "item_count", nullable = false)
    private Integer itemCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "inventoryInquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryInquiryItem> items;

    public enum InquiryStatus {
        SENT
    }
}

