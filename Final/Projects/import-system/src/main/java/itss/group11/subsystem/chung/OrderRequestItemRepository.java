package itss.group11.subsystem.chung;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itss.group11.entity.chung.OrderRequestItem;

@Repository
public interface OrderRequestItemRepository extends JpaRepository<OrderRequestItem, Long> {
}

