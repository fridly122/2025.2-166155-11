package itss.group11.repository.requestManage;

import itss.group11.models.OrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {
    
    // Hàm này giúp Service của bạn tìm kiếm Yêu cầu dựa trên mã code (VD: REQ-001)
    Optional<OrderRequest> findByRequestCode(String requestCode);
}