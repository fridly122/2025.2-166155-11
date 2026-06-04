package itss.group11.subsystem.chung;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itss.group11.entity.chung.PurchaseOrder;
import itss.group11.entity.chung.PurchaseOrderLine;

@Repository
public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, Long> {

    List<PurchaseOrderLine> findByPurchaseOrderOrderByIdAsc(PurchaseOrder purchaseOrder);
}

