package itss.group11.subsystem.chung;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import itss.group11.entity.uc5.SiteStockDTO;
import itss.group11.entity.chung.ImportSite;

@Repository
public interface ImportSiteRepository extends JpaRepository<ImportSite, String> {
    
    Optional<ImportSite> findBySiteCode(String siteCode);
    List<ImportSite> findAllByOrderBySiteCodeAsc();

    /**
     * Truy váº¥n PostgreSQL tÃ­nh tá»•ng sá»‘ lÆ°á»£ng tá»“n kho cá»§a má»™t máº·t hÃ ng trÃªn toÃ n bá»™ há»‡ thá»‘ng cÃ¡c Site
     */
    @Query(value = "SELECT COALESCE(SUM(in_stock_quantity), 0) FROM site_inventory WHERE merchandise_code = :merchandiseCode", nativeQuery = true)
    int calculateTotalStockByItem(@Param("merchandiseCode") String merchandiseCode);

    /**
     * Truy váº¥n láº¥y danh sÃ¡ch chi tiáº¿t tá»“n kho cá»§a máº·t hÃ ng táº¡i tá»«ng Site cá»¥ thá»ƒ, map trá»±c tiáº¿p vÃ o DTO
     */
    @Query(value = "SELECT new itss.group11.entity.uc5.SiteStockDTO(s.siteCode, s.siteName, si.merchandise.code, si.inStockQuantity, s.daysByShip, s.daysByAir) " +
                   "FROM SiteInventory si JOIN si.importSite s " +
                   "WHERE si.merchandise.code = :merchandiseCode AND si.inStockQuantity > 0 " +
                   "ORDER BY CASE WHEN s.daysByShip IS NOT NULL THEN 0 ELSE 1 END, si.inStockQuantity DESC, s.siteCode ASC")
    List<SiteStockDTO> findStockDetailsByItem(@Param("merchandiseCode") String merchandiseCode);

    @Query("""
            SELECT DISTINCT s
            FROM ImportSite s
            JOIN s.merchandiseList m
            WHERE m.code = :merchandiseCode
            ORDER BY s.siteCode ASC
            """)
    List<ImportSite> findSitesSellingMerchandise(@Param("merchandiseCode") String merchandiseCode);
}

