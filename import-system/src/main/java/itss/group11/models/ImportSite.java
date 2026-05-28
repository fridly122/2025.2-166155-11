package itss.group11.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "import_site")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ImportSite {

    @Id
    @Column(name = "site_code", length = 50)
    private String siteCode;           // Mã site (VD: "SITE_JP_01")

    @Column(name = "site_name", nullable = false, length = 100)
    private String siteName;           // Tên site (VD: "Kho Tokyo")

    @Column(name = "days_by_ship")
    private Integer daysByShip;        // Số ngày vận chuyển bằng tàu

    @Column(name = "days_by_air")
    private Integer daysByAir;         // Số ngày vận chuyển bằng hàng không

    @Column(name = "other_info", length = 500)
    private String otherInfo;          // Thông tin khác

    // Danh sách mặt hàng site này kinh doanh
    @ManyToMany
    @JoinTable(
        name = "site_merchandise",
        joinColumns = @JoinColumn(name = "site_code"),
        inverseJoinColumns = @JoinColumn(name = "merchandise_code")
    )
    private List<Merchandise> merchandiseList;
}