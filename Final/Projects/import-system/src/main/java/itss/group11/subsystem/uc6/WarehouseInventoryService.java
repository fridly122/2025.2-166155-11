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
        adjustInternalInventory(merchandise, receivedQty);
    }

    public void adjustInternalInventory(Merchandise merchandise, int quantityDelta) {
        if (merchandise == null || quantityDelta == 0) {
            return;
        }

        InternalWarehouseInventory inventory = internalWarehouseInventoryRepository
                .findByMerchandise_Code(merchandise.getCode())
                .orElseGet(() -> InternalWarehouseInventory.builder()
                        .merchandise(merchandise)
                        .inStockQuantity(0)
                        .build());

        int currentQuantity = inventory.getInStockQuantity() == null ? 0 : inventory.getInStockQuantity();
        int updatedQuantity = currentQuantity + quantityDelta;
        if (updatedQuantity < 0) {
            throw new RuntimeException("Ton kho noi bo khong du de cap nhat so luong thuc nhan cho mat hang: "
                    + merchandise.getCode());
        }

        inventory.setInStockQuantity(updatedQuantity);
        internalWarehouseInventoryRepository.save(inventory);
    }
}
