package itss.group11.entity.chung;

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
    private String siteCode;           // MÃ£ site (VD: "SITE_JP_01")

    @Column(name = "site_name", nullable = false, length = 100)
    private String siteName;           // TÃªn site (VD: "Kho Tokyo")

    @Column(name = "days_by_ship")
    private Integer daysByShip;        // Sá»‘ ngÃ y váº­n chuyá»ƒn báº±ng tÃ u

    @Column(name = "days_by_air")
    private Integer daysByAir;         // Sá»‘ ngÃ y váº­n chuyá»ƒn báº±ng hÃ ng khÃ´ng

    @Column(name = "other_info", length = 500)
    private String otherInfo;          // ThÃ´ng tin khÃ¡c

    // Danh sÃ¡ch máº·t hÃ ng site nÃ y kinh doanh
    @ManyToMany
    @JoinTable(
        name = "site_merchandise",
        joinColumns = @JoinColumn(name = "site_code"),
        inverseJoinColumns = @JoinColumn(name = "merchandise_code")
    )
    private List<Merchandise> merchandiseList;
}
