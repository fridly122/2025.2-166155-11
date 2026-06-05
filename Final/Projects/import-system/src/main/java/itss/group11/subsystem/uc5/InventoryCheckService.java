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
     * Kiểm tra tổng số lượng tồn kho của một mặt hàng trên tất cả các Site (DB Thật)
     */
    public int getTotalStock(String merchandiseCode) {
        return importSiteRepository.calculateTotalStockByItem(merchandiseCode);
    }

    /**
     * Lấy danh sách chi tiết kho của mặt hàng phục vụ thuật toán phân bổ tối ưu
     */
    public List<SiteStockDTO> getStockDetailsAcrossSites(String merchandiseCode) {
        return importSiteRepository.findStockDetailsByItem(merchandiseCode);
    }

    /**
     * Kiểm tra xem tổng lượng tồn kho trên hệ thống có đủ đáp ứng lượng đặt hàng không
     */
    public boolean isStockSufficient(String merchandiseCode, int requiredQuantity) {
        return getTotalStock(merchandiseCode) >= requiredQuantity;
    }
}
