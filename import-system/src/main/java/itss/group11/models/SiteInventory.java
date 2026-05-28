package itss.group11.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "site_inventory")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SiteInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_code", referencedColumnName = "site_code", nullable = false)
    private ImportSite importSite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchandise_code", referencedColumnName = "code", nullable = false)
    private Merchandise merchandise;
    @Column(name = "in_stock_quantity", nullable = false)
    private Integer inStockQuantity; // Số lượng tồn kho thực tế
}