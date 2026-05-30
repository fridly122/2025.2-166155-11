package itss.group11.repository.orderExecution;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itss.group11.models.PurchaseOrder;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {

    Optional<PurchaseOrder> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    List<PurchaseOrder> findByStatusOrderByCreatedAtDesc(PurchaseOrder.PurchaseOrderStatus status);
    List<PurchaseOrder> findByStatusOrderByCreatedAtAsc(PurchaseOrder.PurchaseOrderStatus status);
}
