package itss.group11.repository.orderExecution;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itss.group11.models.PurchaseOrder;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByPoCode(String poCode);
    
    // Kiểm tra xem mã PO đã tồn tại chưa để tự động sinh mã mới
    boolean existsByPoCode(String poCode); 
}