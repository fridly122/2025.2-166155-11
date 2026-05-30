package itss.group11.repository.requestManage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itss.group11.models.OrderRequestItem;

@Repository
public interface OrderRequestItemRepository extends JpaRepository<OrderRequestItem, Long> {
}
