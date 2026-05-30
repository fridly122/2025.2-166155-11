package itss.group11.repository.warehouse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itss.group11.models.DiscrepancyReport;
import itss.group11.models.PurchaseOrder;

@Repository
public interface DiscrepancyReportRepository extends JpaRepository<DiscrepancyReport, String> {

    List<DiscrepancyReport> findByPurchaseOrderOrderByCreatedAtDesc(PurchaseOrder purchaseOrder);
}
