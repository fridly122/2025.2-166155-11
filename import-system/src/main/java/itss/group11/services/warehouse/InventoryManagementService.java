package itss.group11.services.warehouse;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itss.group11.dto.requestManage.MerchandiseOptionDTO;
import itss.group11.dto.warehouse.InternalInventoryRowDTO;
import itss.group11.dto.warehouse.InventoryRowDTO;
import itss.group11.dto.warehouse.InventoryUpdateDTO;
import itss.group11.dto.warehouse.SiteOptionDTO;
import itss.group11.models.ImportSite;
import itss.group11.models.InternalWarehouseInventory;
import itss.group11.models.Merchandise;
import itss.group11.models.SiteInventory;
import itss.group11.repository.allocation.ImportSiteRepository;
import itss.group11.repository.siteSync.MerchandiseRepository;
import itss.group11.repository.siteSync.SiteInventoryRepository;
import itss.group11.repository.warehouse.InternalWarehouseInventoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryManagementService {

    private static final String IN_STOCK = "CÒN HÀNG";
    private static final String OUT_OF_STOCK = "HẾT HÀNG";

    private final SiteInventoryRepository siteInventoryRepository;
    private final ImportSiteRepository importSiteRepository;
    private final MerchandiseRepository merchandiseRepository;
    private final InternalWarehouseInventoryRepository internalWarehouseInventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryRowDTO> getInventoryRows() {
        return siteInventoryRepository.findAllWithSiteAndMerchandise()
                .stream()
                .map(this::toInventoryRowDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InternalInventoryRowDTO> getInternalInventoryRows() {
        return internalWarehouseInventoryRepository.findAllWithMerchandiseOrderByCodeAsc()
                .stream()
                .map(this::toInternalInventoryRowDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SiteOptionDTO> getSiteOptions() {
        return importSiteRepository.findAllByOrderBySiteCodeAsc()
                .stream()
                .map(site -> SiteOptionDTO.builder()
                        .siteCode(site.getSiteCode())
                        .siteName(site.getSiteName())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MerchandiseOptionDTO> getMerchandiseOptions() {
        return merchandiseRepository.findAllByOrderByCodeAsc()
                .stream()
                .map(merchandise -> MerchandiseOptionDTO.builder()
                        .code(merchandise.getCode())
                        .name(merchandise.getName())
                        .unit(merchandise.getUnit())
                        .build())
                .toList();
    }

    @Transactional
    public InventoryRowDTO updateInventory(InventoryUpdateDTO dto) {
        validateUpdateDTO(dto);

        String siteCode = dto.getSiteCode().trim().toUpperCase();
        String merchandiseCode = dto.getMerchandiseCode().trim().toUpperCase();

        ImportSite site = importSiteRepository.findBySiteCode(siteCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy site: " + siteCode));

        Merchandise merchandise = merchandiseRepository.findByCode(merchandiseCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mặt hàng: " + merchandiseCode));

        SiteInventory inventory = siteInventoryRepository
                .findBySiteCodeAndMerchandiseCode(siteCode, merchandiseCode)
                .orElseGet(() -> SiteInventory.builder()
                        .importSite(site)
                        .merchandise(merchandise)
                        .build());

        inventory.setInStockQuantity(dto.getInStockQuantity());
        ensureSiteSellsMerchandise(site, merchandise);

        return toInventoryRowDTO(siteInventoryRepository.save(inventory));
    }

    private void validateUpdateDTO(InventoryUpdateDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Dữ liệu tồn kho không được để trống.");
        }

        if (dto.getSiteCode() == null || dto.getSiteCode().isBlank()) {
            throw new RuntimeException("Mã site không được để trống.");
        }

        if (dto.getMerchandiseCode() == null || dto.getMerchandiseCode().isBlank()) {
            throw new RuntimeException("Mã mặt hàng không được để trống.");
        }

        if (dto.getInStockQuantity() == null || dto.getInStockQuantity() < 0) {
            throw new RuntimeException("Số lượng tồn kho phải lớn hơn hoặc bằng 0.");
        }
    }

    private void ensureSiteSellsMerchandise(ImportSite site, Merchandise merchandise) {
        if (site.getMerchandiseList() == null) {
            site.setMerchandiseList(new ArrayList<>());
        }

        boolean alreadyExists = site.getMerchandiseList()
                .stream()
                .anyMatch(existing -> merchandise.getCode().equals(existing.getCode()));

        if (!alreadyExists) {
            site.getMerchandiseList().add(merchandise);
            importSiteRepository.save(site);
        }
    }

    private InventoryRowDTO toInventoryRowDTO(SiteInventory inventory) {
        ImportSite site = inventory.getImportSite();
        Merchandise merchandise = inventory.getMerchandise();
        Integer quantity = inventory.getInStockQuantity() == null ? 0 : inventory.getInStockQuantity();

        return InventoryRowDTO.builder()
                .id(inventory.getId())
                .siteCode(site == null ? "" : site.getSiteCode())
                .siteName(site == null ? "" : site.getSiteName())
                .merchandiseCode(merchandise == null ? "" : merchandise.getCode())
                .merchandiseName(merchandise == null ? "" : merchandise.getName())
                .unit(merchandise == null ? "" : merchandise.getUnit())
                .inStockQuantity(quantity)
                .stockStatus(quantity > 0 ? IN_STOCK : OUT_OF_STOCK)
                .build();
    }

    private InternalInventoryRowDTO toInternalInventoryRowDTO(InternalWarehouseInventory inventory) {
        Merchandise merchandise = inventory.getMerchandise();
        Integer quantity = inventory.getInStockQuantity() == null ? 0 : inventory.getInStockQuantity();

        return InternalInventoryRowDTO.builder()
                .id(inventory.getId())
                .merchandiseCode(merchandise == null ? "" : merchandise.getCode())
                .merchandiseName(merchandise == null ? "" : merchandise.getName())
                .unit(merchandise == null ? "" : merchandise.getUnit())
                .inStockQuantity(quantity)
                .stockStatus(quantity > 0 ? IN_STOCK : OUT_OF_STOCK)
                .updatedAt(inventory.getUpdatedAt() == null ? "" : inventory.getUpdatedAt().toString())
                .build();
    }
}
