package itss.group11.repository.orderExecution;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itss.group11.models.PurchaseOrder;
import itss.group11.models.PurchaseOrderLine;

@Repository
public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, Long> {

    List<PurchaseOrderLine> findByPurchaseOrderOrderByIdAsc(PurchaseOrder purchaseOrder);
}
