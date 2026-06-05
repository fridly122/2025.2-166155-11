package itss.group11.subsystem.chung;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import itss.group11.entity.chung.InternalWarehouseInventory;

@Repository
public interface InternalWarehouseInventoryRepository extends JpaRepository<InternalWarehouseInventory, Long> {

    Optional<InternalWarehouseInventory> findByMerchandise_Code(String merchandiseCode);

    @Query("""
            SELECT inventory
            FROM InternalWarehouseInventory inventory
            JOIN FETCH inventory.merchandise merchandise
            ORDER BY merchandise.code ASC
            """)
    List<InternalWarehouseInventory> findAllWithMerchandiseOrderByCodeAsc();
}

