package itss.group11.subsystem.uc6;

import org.springframework.stereotype.Service;

import itss.group11.entity.chung.InternalWarehouseInventory;
import itss.group11.entity.chung.Merchandise;
import itss.group11.subsystem.chung.InternalWarehouseInventoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WarehouseInventoryService {

    private final InternalWarehouseInventoryRepository internalWarehouseInventoryRepository;

    public void increaseInternalInventory(Merchandise merchandise, int receivedQty) {
        if (merchandise == null || receivedQty <= 0) {
            return;
        }

        InternalWarehouseInventory inventory = internalWarehouseInventoryRepository
                .findByMerchandise_Code(merchandise.getCode())
                .orElseGet(() -> InternalWarehouseInventory.builder()
                        .merchandise(merchandise)
                        .inStockQuantity(0)
                        .build());

        int currentQuantity = inventory.getInStockQuantity() == null ? 0 : inventory.getInStockQuantity();
        inventory.setInStockQuantity(currentQuantity + receivedQty);
        internalWarehouseInventoryRepository.save(inventory);
    }
}
