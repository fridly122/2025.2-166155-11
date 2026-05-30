package itss.group11.repository.siteSync;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itss.group11.models.InventoryInquiry;

@Repository
public interface InventoryInquiryRepository extends JpaRepository<InventoryInquiry, String> {
}
