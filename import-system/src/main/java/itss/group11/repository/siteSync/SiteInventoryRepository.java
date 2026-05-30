package itss.group11.repository.siteSync;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import itss.group11.models.SiteInventory;

@Repository
public interface SiteInventoryRepository extends JpaRepository<SiteInventory, Long> {

    @Query("""
            SELECT si
            FROM SiteInventory si
            JOIN FETCH si.importSite s
            JOIN FETCH si.merchandise m
            ORDER BY s.siteCode ASC, m.code ASC
            """)
    List<SiteInventory> findAllWithSiteAndMerchandise();

    @Query("""
            SELECT si
            FROM SiteInventory si
            JOIN FETCH si.importSite s
            JOIN FETCH si.merchandise m
            WHERE m.code = :merchandiseCode
            ORDER BY si.inStockQuantity DESC, s.siteCode ASC
            """)
    List<SiteInventory> findByMerchandiseCode(@Param("merchandiseCode") String merchandiseCode);

    @Query("""
            SELECT si
            FROM SiteInventory si
            JOIN FETCH si.importSite s
            JOIN FETCH si.merchandise m
            WHERE s.siteCode = :siteCode AND m.code = :merchandiseCode
            """)
    Optional<SiteInventory> findBySiteCodeAndMerchandiseCode(
            @Param("siteCode") String siteCode,
            @Param("merchandiseCode") String merchandiseCode
    );
}
