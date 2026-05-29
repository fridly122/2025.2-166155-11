package itss.group11.repository.requestManage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itss.group11.models.OrderRequest;

@Repository
public interface OrderRequestRepository extends JpaRepository<OrderRequest, String> {

    Optional<OrderRequest> findByRequestCode(String requestCode);
    List<OrderRequest> findByStatusOrderByCreatedAtDesc(OrderRequest.OrderRequestStatus status);
}