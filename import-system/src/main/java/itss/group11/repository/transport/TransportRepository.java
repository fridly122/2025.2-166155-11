package itss.group11.repository.transport;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import itss.group11.models.PurchaseOrder;
import itss.group11.models.TransportInfo;

@Repository
public interface TransportRepository extends JpaRepository<TransportInfo, Long> {

    List<TransportInfo> findAllByOrderByIdDesc();
    List<TransportInfo> findAllByOrderByIdAsc();

    @Query("""
            SELECT t
            FROM TransportInfo t
            LEFT JOIN FETCH t.purchaseOrder po
            LEFT JOIN FETCH po.orderRequest
            LEFT JOIN FETCH t.sourceSite
            ORDER BY po.createdAt ASC, t.id ASC
            """)
    List<TransportInfo> findAllOrderByPurchaseOrderCreatedAtAsc();

    Optional<TransportInfo> findByPurchaseOrder(PurchaseOrder purchaseOrder);

    boolean existsByPurchaseOrder(PurchaseOrder purchaseOrder);
}
