package itss.group11.subsystem.chung;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itss.group11.entity.chung.InventoryInquiry;

@Repository
public interface InventoryInquiryRepository extends JpaRepository<InventoryInquiry, String> {

    boolean existsByOrderRequest_RequestCode(String requestCode);
}

