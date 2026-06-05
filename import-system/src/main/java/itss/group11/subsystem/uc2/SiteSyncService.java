package itss.group11.subsystem.uc2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itss.group11.entity.chung.ImportSite;
import itss.group11.entity.chung.InventoryInquiry;
import itss.group11.entity.chung.InventoryInquiryItem;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.entity.chung.OrderRequestItem;
import itss.group11.entity.uc1.MerchandiseOptionDTO;
import itss.group11.entity.uc1.OrderRequestDetailDTO;
import itss.group11.entity.uc1.OrderRequestSummaryDTO;
import itss.group11.entity.uc2.InventoryInquiryRequestDTO;
import itss.group11.entity.uc2.InventoryInquiryResponseDTO;
import itss.group11.entity.uc2.InventoryInquirySendResultDTO;
import itss.group11.entity.uc2.OrderRequestClassificationDTO;
import itss.group11.entity.uc2.SiteClassificationResultDTO;
import itss.group11.subsystem.chung.ImportSiteRepository;
import itss.group11.subsystem.chung.InventoryInquiryRepository;
import itss.group11.subsystem.chung.MerchandiseRepository;
import itss.group11.subsystem.chung.OrderRequestRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SiteSyncService {

    private final MerchandiseRepository merchandiseRepository;
    private final ImportSiteRepository importSiteRepository;
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

    if (request.getStatus() != OrderRequest.OrderRequestStatus.PENDING) {
        throw new RuntimeException("Chỉ được phân loại yêu cầu ở trạng thái PENDING.");
    }

    if (request.getItems() == null || request.getItems().isEmpty()) {
        throw new RuntimeException("Yêu cầu nhập hàng chưa có mặt hàng để phân loại.");
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
        if (classification.getResults() == null || classification.getResults().isEmpty()) {
    throw new RuntimeException("Không có site phù hợp để gửi yêu cầu hỏi tồn kho.");
}       
        
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
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy site: " + entry.getKey()));

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
                .message("Đã lưu " + inquiryIds.size()
                        + " phiếu hỏi tồn kho cho yêu cầu " + classification.getRequestCode()
                        + " vào database.")
                .build();
    }

    @Transactional(readOnly = true)
    public InventoryInquiryResponseDTO classifySites(InventoryInquiryRequestDTO requestDTO) {
        validateRequest(requestDTO);

        String merchandiseCode = requestDTO.getMerchandiseCode().trim().toUpperCase();
        int requiredQuantity = requestDTO.getRequiredQuantity();

        Merchandise merchandise = merchandiseRepository.findByCode(merchandiseCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mặt hàng: " + merchandiseCode));

        return classifyItem(merchandise, requiredQuantity);
    }

    private InventoryInquiryResponseDTO classifyItem(Merchandise merchandise, int requiredQuantity) {
    List<ImportSite> sitesSellingMerchandise =
            importSiteRepository.findSitesSellingMerchandise(merchandise.getCode());

    List<SiteClassificationResultDTO> results = sitesSellingMerchandise
            .stream()
            .map(site -> toResultDTO(site, merchandise, requiredQuantity))
            .toList();

    return InventoryInquiryResponseDTO.builder()
            .merchandiseCode(merchandise.getCode())
            .merchandiseName(merchandise.getName())
            .unit(merchandise.getUnit())
            .requiredQuantity(requiredQuantity)
            .results(results)
            .message(buildMessage(merchandise, results))
            .build();
}

    private void validateRequest(InventoryInquiryRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new RuntimeException("Dữ liệu tìm kiếm không được để trống.");
        }

        if (requestDTO.getMerchandiseCode() == null || requestDTO.getMerchandiseCode().isBlank()) {
            throw new RuntimeException("Vui lòng chọn mặt hàng cần tìm site.");
        }

        if (requestDTO.getRequiredQuantity() == null || requestDTO.getRequiredQuantity() <= 0) {
            throw new RuntimeException("Số lượng cần kiểm tra phải lớn hơn 0.");
        }
    }

    private OrderRequest findRequest(String requestCode) {
        if (requestCode == null || requestCode.isBlank()) {
            throw new RuntimeException("Mã yêu cầu nhập hàng không được để trống.");
        }

        return orderRequestRepository.findByRequestCode(requestCode.trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhập hàng: " + requestCode));
    }

    private InventoryInquiryItem toInventoryInquiryItem(
            InventoryInquiry inquiry,
            SiteClassificationResultDTO result
    ) {
        Merchandise merchandise = merchandiseRepository.findByCode(result.getMerchandiseCode())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mặt hàng: "
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
        int requiredQuantity
) {
    return SiteClassificationResultDTO.builder()
            .siteCode(site.getSiteCode())
            .siteName(site.getSiteName())
            .merchandiseCode(merchandise.getCode())
            .merchandiseName(merchandise.getName())
            .unit(merchandise.getUnit())
            .requiredQuantity(requiredQuantity)
            .status("Sẽ gửi hỏi tồn kho")
            .build();
}

    

    private String buildMessage(
        Merchandise merchandise,
        List<SiteClassificationResultDTO> results
) {
    if (results.isEmpty()) {
        return "Không có site nào kinh doanh mặt hàng " + merchandise.getCode() + ".";
    }

    return "Đã tìm thấy " + results.size()
            + " site kinh doanh mặt hàng " + merchandise.getCode()
            + ".";
}

    private String buildRequestClassificationMessage(String requestCode, int itemCount, int siteCount) {
        if (siteCount == 0) {
            return "Không tìm thấy site nào kinh doanh các mặt hàng trong yêu cầu " + requestCode + ".";
        }

        return "Đã phân loại " + itemCount + " mặt hàng trong yêu cầu " + requestCode
                + " theo " + siteCount + " site phù hợp.";
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


}

