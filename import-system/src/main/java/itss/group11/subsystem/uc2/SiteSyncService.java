package itss.group11.subsystem.uc2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itss.group11.entity.uc1.MerchandiseOptionDTO;
import itss.group11.entity.uc1.OrderRequestDetailDTO;
import itss.group11.entity.uc1.OrderRequestSummaryDTO;
import itss.group11.entity.uc2.InventoryInquiryRequestDTO;
import itss.group11.entity.uc2.InventoryInquiryResponseDTO;
import itss.group11.entity.uc2.InventoryInquirySendResultDTO;
import itss.group11.entity.uc2.OrderRequestClassificationDTO;
import itss.group11.entity.uc2.SiteClassificationResultDTO;
import itss.group11.entity.chung.ImportSite;
import itss.group11.entity.chung.InventoryInquiry;
import itss.group11.entity.chung.InventoryInquiryItem;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.entity.chung.OrderRequestItem;
import itss.group11.subsystem.chung.ImportSiteRepository;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.chung.InventoryInquiryRepository;
import itss.group11.subsystem.chung.MerchandiseRepository;
import itss.group11.subsystem.chung.SiteInventoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SiteSyncService {

    private static final String CLASS_ENOUGH = "Äá»¦ HÃ€NG";
    private static final String CLASS_PARTIAL = "ÄÃP á»¨NG Má»˜T PHáº¦N";
    private static final String CLASS_EMPTY = "CHÆ¯A CÃ“ Tá»’N KHO";

    private final MerchandiseRepository merchandiseRepository;
    private final ImportSiteRepository importSiteRepository;
    private final SiteInventoryRepository siteInventoryRepository;
    private final OrderRequestRepository orderRequestRepository;
    private final InventoryInquiryRepository inventoryInquiryRepository;

    @Transactional(readOnly = true)
    public List<OrderRequestSummaryDTO> getPendingRequests() {
        return orderRequestRepository
                .findByStatusOrderByDesiredDeliveryDateAscCreatedAtAsc(OrderRequest.OrderRequestStatus.PENDING)
                .stream()
                .map(this::toRequestSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderRequestDetailDTO getRequestDetail(String requestCode) {
        return toRequestDetailDTO(findRequest(requestCode));
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

    @Transactional(readOnly = true)
    public OrderRequestClassificationDTO classifyOrderRequest(String requestCode) {
        OrderRequest request = findRequest(requestCode);
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("YÃªu cáº§u nháº­p hÃ ng chÆ°a cÃ³ máº·t hÃ ng Ä‘á»ƒ phÃ¢n loáº¡i.");
        }

        List<OrderRequestDetailDTO.ItemDTO> items = request.getItems()
                .stream()
                .map(this::toRequestItemDTO)
                .toList();

        List<SiteClassificationResultDTO> results = request.getItems()
                .stream()
                .flatMap(item -> classifyItem(item.getMerchandise(), item.getQuantityOrdered())
                        .getResults()
                        .stream())
                .toList();

        int siteCount = (int) results.stream()
                .map(SiteClassificationResultDTO::getSiteCode)
                .distinct()
                .count();

        return OrderRequestClassificationDTO.builder()
                .requestCode(request.getRequestCode())
                .status(request.getStatus() == null ? "" : request.getStatus().name())
                .desiredDeliveryDate(request.getDesiredDeliveryDate() == null
                        ? ""
                        : request.getDesiredDeliveryDate().toString())
                .itemCount(items.size())
                .siteCount(siteCount)
                .items(items)
                .results(results)
                .message(buildRequestClassificationMessage(request.getRequestCode(), items.size(), siteCount))
                .build();
    }

    @Transactional
    public InventoryInquirySendResultDTO sendInventoryInquiry(String requestCode) {
        OrderRequest request = findRequest(requestCode);
        OrderRequestClassificationDTO classification = classifyOrderRequest(requestCode);
        Map<String, List<SiteClassificationResultDTO>> resultsBySite = classification.getResults()
                .stream()
                .collect(Collectors.groupingBy(
                        SiteClassificationResultDTO::getSiteCode,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<String> inquiryIds = new ArrayList<>();

        for (Map.Entry<String, List<SiteClassificationResultDTO>> entry : resultsBySite.entrySet()) {
            ImportSite site = importSiteRepository.findBySiteCode(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y site: " + entry.getKey()));

            InventoryInquiry inquiry = InventoryInquiry.builder()
                    .inquiryId(generateInquiryId())
                    .orderRequest(request)
                    .importSite(site)
                    .status(InventoryInquiry.InquiryStatus.SENT)
                    .itemCount(entry.getValue().size())
                    .build();

            List<InventoryInquiryItem> items = entry.getValue()
                    .stream()
                    .map(result -> toInventoryInquiryItem(inquiry, result))
                    .toList();

            inquiry.setItems(items);
            inquiryIds.add(inventoryInquiryRepository.save(inquiry).getInquiryId());
        }

        return InventoryInquirySendResultDTO.builder()
                .requestCode(classification.getRequestCode())
                .itemCount(classification.getItemCount())
                .siteCount(inquiryIds.size())
                .inquiryIds(inquiryIds)
                .message("ÄÃ£ lÆ°u " + inquiryIds.size()
                        + " phiáº¿u há»i tá»“n kho cho yÃªu cáº§u " + classification.getRequestCode()
                        + " vÃ o database.")
                .build();
    }

    @Transactional(readOnly = true)
    public InventoryInquiryResponseDTO classifySites(InventoryInquiryRequestDTO requestDTO) {
        validateRequest(requestDTO);

        String merchandiseCode = requestDTO.getMerchandiseCode().trim().toUpperCase();
        int requiredQuantity = requestDTO.getRequiredQuantity();

        Merchandise merchandise = merchandiseRepository.findByCode(merchandiseCode)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y máº·t hÃ ng: " + merchandiseCode));

        return classifyItem(merchandise, requiredQuantity);
    }

    private InventoryInquiryResponseDTO classifyItem(Merchandise merchandise, int requiredQuantity) {
        List<ImportSite> sitesSellingMerchandise =
                importSiteRepository.findSitesSellingMerchandise(merchandise.getCode());

        Map<String, Integer> inventoryBySiteCode =
                siteInventoryRepository.findByMerchandiseCode(merchandise.getCode())
                        .stream()
                        .collect(Collectors.toMap(
                                inventory -> inventory.getImportSite().getSiteCode(),
                                inventory -> inventory.getInStockQuantity() == null ? 0 : inventory.getInStockQuantity()
                        ));

        List<SiteClassificationResultDTO> results = sitesSellingMerchandise
                .stream()
                .map(site -> toResultDTO(site, merchandise, requiredQuantity,
                        inventoryBySiteCode.getOrDefault(site.getSiteCode(), 0)))
                .sorted(Comparator
                        .comparing((SiteClassificationResultDTO result) ->
                                result.getInStockQuantity() >= requiredQuantity ? 0 : result.getInStockQuantity() > 0 ? 1 : 2)
                        .thenComparing(SiteClassificationResultDTO::getInStockQuantity, Comparator.reverseOrder())
                        .thenComparing(SiteClassificationResultDTO::getSiteCode)
                        .thenComparing(SiteClassificationResultDTO::getMerchandiseCode))
                .toList();

        int totalStock = results.stream()
                .map(SiteClassificationResultDTO::getInStockQuantity)
                .reduce(0, Integer::sum);

        return InventoryInquiryResponseDTO.builder()
                .merchandiseCode(merchandise.getCode())
                .merchandiseName(merchandise.getName())
                .unit(merchandise.getUnit())
                .requiredQuantity(requiredQuantity)
                .totalStock(totalStock)
                .enoughInventory(totalStock >= requiredQuantity)
                .message(buildMessage(merchandise, requiredQuantity, totalStock, results))
                .results(results)
                .build();
    }

    private void validateRequest(InventoryInquiryRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new RuntimeException("Dá»¯ liá»‡u tÃ¬m kiáº¿m khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        }

        if (requestDTO.getMerchandiseCode() == null || requestDTO.getMerchandiseCode().isBlank()) {
            throw new RuntimeException("Vui lÃ²ng chá»n máº·t hÃ ng cáº§n tÃ¬m site.");
        }

        if (requestDTO.getRequiredQuantity() == null || requestDTO.getRequiredQuantity() <= 0) {
            throw new RuntimeException("Sá»‘ lÆ°á»£ng cáº§n kiá»ƒm tra pháº£i lá»›n hÆ¡n 0.");
        }
    }

    private OrderRequest findRequest(String requestCode) {
        if (requestCode == null || requestCode.isBlank()) {
            throw new RuntimeException("MÃ£ yÃªu cáº§u nháº­p hÃ ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        }

        return orderRequestRepository.findByRequestCode(requestCode.trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y yÃªu cáº§u nháº­p hÃ ng: " + requestCode));
    }

    private InventoryInquiryItem toInventoryInquiryItem(
            InventoryInquiry inquiry,
            SiteClassificationResultDTO result
    ) {
        Merchandise merchandise = merchandiseRepository.findByCode(result.getMerchandiseCode())
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y máº·t hÃ ng: "
                        + result.getMerchandiseCode()));

        return InventoryInquiryItem.builder()
                .inventoryInquiry(inquiry)
                .merchandise(merchandise)
                .requestedQty(result.getRequiredQuantity())
                .unit(result.getUnit())
                .build();
    }

    private String generateInquiryId() {
        return "INQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private SiteClassificationResultDTO toResultDTO(
            ImportSite site,
            Merchandise merchandise,
            int requiredQuantity,
            int inStockQuantity
    ) {
        return SiteClassificationResultDTO.builder()
                .siteCode(site.getSiteCode())
                .siteName(site.getSiteName())
                .merchandiseCode(merchandise.getCode())
                .merchandiseName(merchandise.getName())
                .unit(merchandise.getUnit())
                .requiredQuantity(requiredQuantity)
                .inStockQuantity(inStockQuantity)
                .classification(classifyStock(inStockQuantity, requiredQuantity))
                .suggestedTransportMeans(suggestTransportMeans(site))
                .estimatedDeliveryDays(suggestDeliveryDays(site))
                .daysByAir(site.getDaysByAir())
                .daysByShip(site.getDaysByShip())
                .otherInfo(site.getOtherInfo())
                .build();
    }

    private String classifyStock(int inStockQuantity, int requiredQuantity) {
        if (inStockQuantity >= requiredQuantity) {
            return CLASS_ENOUGH;
        }

        if (inStockQuantity > 0) {
            return CLASS_PARTIAL;
        }

        return CLASS_EMPTY;
    }

    private String suggestTransportMeans(ImportSite site) {
        Integer daysByShip = site.getDaysByShip();
        Integer daysByAir = site.getDaysByAir();

        if (daysByShip != null) {
            return "SHIP";
        }

        if (daysByAir != null) {
            return "AIR";
        }

        return "N/A";
    }

    private Integer suggestDeliveryDays(ImportSite site) {
        String means = suggestTransportMeans(site);

        if ("AIR".equals(means)) {
            return site.getDaysByAir();
        }

        if ("SHIP".equals(means)) {
            return site.getDaysByShip();
        }

        return null;
    }

    private String buildMessage(
            Merchandise merchandise,
            int requiredQuantity,
            int totalStock,
            List<SiteClassificationResultDTO> results
    ) {
        if (results.isEmpty()) {
            return "KhÃ´ng cÃ³ site nÃ o Ä‘ang kinh doanh máº·t hÃ ng " + merchandise.getCode() + ".";
        }

        if (totalStock >= requiredQuantity) {
            return "Tá»•ng tá»“n kho Ä‘Ã¡p á»©ng Ä‘á»§ sá»‘ lÆ°á»£ng cáº§n kiá»ƒm tra.";
        }

        return "Tá»•ng tá»“n kho hiá»‡n cÃ³ chÆ°a Ä‘á»§. Cáº§n " + requiredQuantity
                + " " + nullToEmpty(merchandise.getUnit())
                + ", hiá»‡n cÃ³ " + totalStock + ".";
    }

    private String buildRequestClassificationMessage(String requestCode, int itemCount, int siteCount) {
        if (siteCount == 0) {
            return "KhÃ´ng tÃ¬m tháº¥y site nÃ o kinh doanh cÃ¡c máº·t hÃ ng trong yÃªu cáº§u " + requestCode + ".";
        }

        return "ÄÃ£ phÃ¢n loáº¡i " + itemCount + " máº·t hÃ ng trong yÃªu cáº§u " + requestCode
                + " theo " + siteCount + " site phÃ¹ há»£p.";
    }

    private OrderRequestSummaryDTO toRequestSummaryDTO(OrderRequest request) {
        return OrderRequestSummaryDTO.builder()
                .requestCode(request.getRequestCode())
                .status(request.getStatus() == null ? "" : request.getStatus().name())
                .desiredDeliveryDate(request.getDesiredDeliveryDate() == null
                        ? ""
                        : request.getDesiredDeliveryDate().toString())
                .createdDate(request.getCreatedAt() == null
                        ? ""
                        : request.getCreatedAt().toLocalDate().toString())
                .itemCount(request.getItems() == null ? 0 : request.getItems().size())
                .build();
    }

    private OrderRequestDetailDTO toRequestDetailDTO(OrderRequest request) {
        return OrderRequestDetailDTO.builder()
                .requestCode(request.getRequestCode())
                .status(request.getStatus() == null ? "" : request.getStatus().name())
                .desiredDeliveryDate(request.getDesiredDeliveryDate() == null
                        ? ""
                        : request.getDesiredDeliveryDate().toString())
                .createdDate(request.getCreatedAt() == null
                        ? ""
                        : request.getCreatedAt().toLocalDate().toString())
                .items(request.getItems() == null
                        ? List.of()
                        : request.getItems().stream().map(this::toRequestItemDTO).toList())
                .build();
    }

    private OrderRequestDetailDTO.ItemDTO toRequestItemDTO(OrderRequestItem item) {
        Merchandise merchandise = item.getMerchandise();

        return OrderRequestDetailDTO.ItemDTO.builder()
                .id(item.getId())
                .merchandiseCode(merchandise == null ? "" : merchandise.getCode())
                .merchandiseName(merchandise == null ? "" : merchandise.getName())
                .quantityOrdered(item.getQuantityOrdered())
                .unit(merchandise == null ? "" : merchandise.getUnit())
                .build();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

