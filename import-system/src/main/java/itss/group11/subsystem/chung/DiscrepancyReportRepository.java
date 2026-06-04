package itss.group11.subsystem.chung;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itss.group11.entity.chung.DiscrepancyReport;
import itss.group11.entity.chung.PurchaseOrder;

@Repository
public interface DiscrepancyReportRepository extends JpaRepository<DiscrepancyReport, String> {

    List<DiscrepancyReport> findByPurchaseOrderOrderByCreatedAtDesc(PurchaseOrder purchaseOrder);
}

