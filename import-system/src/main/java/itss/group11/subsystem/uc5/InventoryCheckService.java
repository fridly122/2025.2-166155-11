package itss.group11.subsystem.uc5;

import java.util.List;

import org.springframework.stereotype.Service;

import itss.group11.entity.uc5.SiteStockDTO;
import itss.group11.subsystem.chung.ImportSiteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryCheckService {

    private final ImportSiteRepository importSiteRepository;

    /**
     * Kiá»ƒm tra tá»•ng sá»‘ lÆ°á»£ng tá»“n kho cá»§a má»™t máº·t hÃ ng trÃªn táº¥t cáº£ cÃ¡c Site (DB Tháº­t)
     */
    public int getTotalStock(String merchandiseCode) {
        return importSiteRepository.calculateTotalStockByItem(merchandiseCode);
    }

    /**
     * Láº¥y danh sÃ¡ch chi tiáº¿t kho cá»§a máº·t hÃ ng phá»¥c vá»¥ thuáº­t toÃ¡n phÃ¢n bá»• tá»‘i Æ°u
     */
    public List<SiteStockDTO> getStockDetailsAcrossSites(String merchandiseCode) {
        return importSiteRepository.findStockDetailsByItem(merchandiseCode);
    }

    /**
     * Kiá»ƒm tra xem tá»•ng lÆ°á»£ng tá»“n kho trÃªn há»‡ thá»‘ng cÃ³ Ä‘á»§ Ä‘Ã¡p á»©ng lÆ°á»£ng Ä‘áº·t hÃ ng khÃ´ng
     */
    public boolean isStockSufficient(String merchandiseCode, int requiredQuantity) {
        return getTotalStock(merchandiseCode) >= requiredQuantity;
    }
}
