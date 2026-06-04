package itss.group11.subsystem.uc1;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itss.group11.entity.uc1.MerchandiseOptionDTO;
import itss.group11.entity.uc1.OrderRequestCreationDTO;
import itss.group11.entity.uc1.OrderRequestDetailDTO;
import itss.group11.entity.uc1.OrderRequestSummaryDTO;
import itss.group11.entity.chung.Merchandise;
import itss.group11.entity.chung.OrderRequest;
import itss.group11.entity.chung.OrderRequestItem;
import itss.group11.subsystem.chung.OrderRequestRepository;
import itss.group11.subsystem.chung.MerchandiseRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderRequestService {

    private static final Pattern REQUEST_CODE_PATTERN = Pattern.compile("^REQ(\\d+)$");

    private final OrderRequestRepository orderRequestRepository;
    private final MerchandiseRepository merchandiseRepository;

    @Transactional(readOnly = true)
    public List<OrderRequestSummaryDTO> getAllRequests() {
        return orderRequestRepository.findAllByOrderByCreatedAtAsc()
                .stream()
                .map(this::toSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderRequestDetailDTO getRequestDetail(String requestCode) {
        OrderRequest request = orderRequestRepository.findByRequestCode(requestCode)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y yÃªu cáº§u nháº­p hÃ ng: " + requestCode));

        return toDetailDTO(request);
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
    public OrderRequestDetailDTO createRequest(OrderRequestCreationDTO dto) {
        validateCreationDTO(dto);

        String requestCode = normalizeOrGenerateRequestCode(dto.getRequestCode());
        if (orderRequestRepository.existsById(requestCode)) {
            throw new RuntimeException("MÃ£ yÃªu cáº§u Ä‘Ã£ tá»“n táº¡i: " + requestCode);
        }

        LocalDate desiredDeliveryDate = parseDesiredDeliveryDate(dto.getDesiredDeliveryDate());

        OrderRequest request = OrderRequest.builder()
                .requestCode(requestCode)
                .desiredDeliveryDate(desiredDeliveryDate)
                .status(OrderRequest.OrderRequestStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        for (OrderRequestCreationDTO.ItemDTO itemDTO : dto.getItems()) {
            String merchandiseCode = requireText(itemDTO.getMerchandiseCode(), "MÃ£ máº·t hÃ ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
                    .toUpperCase();

            Merchandise merchandise = merchandiseRepository.findByCode(merchandiseCode)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y máº·t hÃ ng: " + merchandiseCode));

            Integer quantity = itemDTO.getQuantityOrdered();
            if (quantity == null || quantity <= 0) {
                throw new RuntimeException("Sá»‘ lÆ°á»£ng Ä‘áº·t pháº£i lá»›n hÆ¡n 0 cho máº·t hÃ ng: " + merchandiseCode);
            }

            OrderRequestItem item = OrderRequestItem.builder()
                    .orderRequest(request)
                    .merchandise(merchandise)
                    .quantityOrdered(quantity)
                    .build();

            request.getItems().add(item);
        }

        OrderRequest savedRequest = orderRequestRepository.save(request);
        return toDetailDTO(savedRequest);
    }

    private void validateCreationDTO(OrderRequestCreationDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Dá»¯ liá»‡u táº¡o yÃªu cáº§u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        }

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("YÃªu cáº§u nháº­p hÃ ng pháº£i cÃ³ Ã­t nháº¥t má»™t máº·t hÃ ng.");
        }
    }

    private String normalizeOrGenerateRequestCode(String requestCode) {
        if (requestCode == null || requestCode.isBlank()) {
            return generateUniqueRequestCode();
        }

        return requestCode.trim().toUpperCase();
    }

    private String generateUniqueRequestCode() {
        int nextNumber = orderRequestRepository.findAllRequestCodes()
                .stream()
                .map(this::extractRequestNumber)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;

        String requestCode = formatRequestCode(nextNumber);
        while (orderRequestRepository.existsById(requestCode)) {
            nextNumber++;
            requestCode = formatRequestCode(nextNumber);
        }

        return requestCode;
    }

    private int extractRequestNumber(String requestCode) {
        if (requestCode == null) {
            return 0;
        }

        Matcher matcher = REQUEST_CODE_PATTERN.matcher(requestCode.trim().toUpperCase());
        if (!matcher.matches()) {
            return 0;
        }

        return Integer.parseInt(matcher.group(1));
    }

    private String formatRequestCode(int number) {
        if (number < 1000) {
            return "REQ" + String.format("%03d", number);
        }

        return "REQ" + number;
    }

    private LocalDate parseDesiredDeliveryDate(String value) {
        String normalizedValue = requireText(value, "NgÃ y mong muá»‘n nháº­n hÃ ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        try {
            LocalDate desiredDeliveryDate = LocalDate.parse(normalizedValue);
            if (!desiredDeliveryDate.isAfter(LocalDate.now())) {
                throw new RuntimeException("NgÃ y mong muá»‘n nháº­n hÃ ng pháº£i sau ngÃ y hiá»‡n táº¡i.");
            }

            return desiredDeliveryDate;
        } catch (DateTimeParseException e) {
            throw new RuntimeException("NgÃ y mong muá»‘n nháº­n hÃ ng pháº£i cÃ³ Ä‘á»‹nh dáº¡ng yyyy-MM-dd.");
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(message);
        }

        return value.trim();
    }

    private OrderRequestSummaryDTO toSummaryDTO(OrderRequest request) {
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

    private OrderRequestDetailDTO toDetailDTO(OrderRequest request) {
        List<OrderRequestDetailDTO.ItemDTO> items = request.getItems() == null
                ? List.of()
                : request.getItems()
                        .stream()
                        .map(this::toDetailItemDTO)
                        .toList();

        return OrderRequestDetailDTO.builder()
                .requestCode(request.getRequestCode())
                .status(request.getStatus() == null ? "" : request.getStatus().name())
                .desiredDeliveryDate(request.getDesiredDeliveryDate() == null
                        ? ""
                        : request.getDesiredDeliveryDate().toString())
                .createdDate(request.getCreatedAt() == null
                        ? ""
                        : request.getCreatedAt().toLocalDate().toString())
                .items(items)
                .build();
    }

    private OrderRequestDetailDTO.ItemDTO toDetailItemDTO(OrderRequestItem item) {
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

