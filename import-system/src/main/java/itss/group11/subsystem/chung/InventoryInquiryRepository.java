package itss.group11.subsystem.chung;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import itss.group11.entity.chung.InventoryInquiry;

@Repository
public interface InventoryInquiryRepository extends JpaRepository<InventoryInquiry, String> {

    boolean existsByOrderRequest_RequestCode(String requestCode);
    @Query("""
            SELECT COUNT(i)
            FROM InventoryInquiry i
            WHERE i.orderRequest.requestCode = :requestCode
              AND i.importSite.siteCode = :siteCode
            """)
    long countByRequestCodeAndSiteCode(
            @Param("requestCode") String requestCode,
            @Param("siteCode") String siteCode
    );
}

