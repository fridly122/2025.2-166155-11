package itss.group11.subsystem.uc6;

import java.util.Optional;

import org.springframework.stereotype.Service;

import itss.group11.entity.chung.DiscrepancyReport;
import itss.group11.entity.chung.PurchaseOrder;
import itss.group11.entity.chung.PurchaseOrderLine;
import itss.group11.entity.uc6.ReconciliationSubmitDTO;
import itss.group11.subsystem.chung.DiscrepancyReportRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscrepancyReportService {

    private final DiscrepancyReportRepository discrepancyReportRepository;
    private final ReportIdGenerator reportIdGenerator;

    public Optional<String> createIfDiscrepant(
            PurchaseOrder purchaseOrder,
            PurchaseOrderLine line,
            int receivedQty,
            ReconciliationSubmitDTO dto
    ) {
        int differenceQty = line.getOrderedQty() - receivedQty;
        if (differenceQty == 0) {
            return Optional.empty();
        }

        DiscrepancyReport report = DiscrepancyReport.builder()
                .reportId(reportIdGenerator.nextId())
                .purchaseOrder(purchaseOrder)
                .merchandise(line.getMerchandise())
                .orderedQty(line.getOrderedQty())
                .receivedQty(receivedQty)
                .differenceQty(differenceQty)
                .reason(normalizeText(dto.getReason(), "Sai lech so luong khi doi soat nhap kho"))
                .note(normalizeText(dto.getNote(), ""))
                .createdBy(normalizeText(dto.getCreatedBy(), "Nhan vien kho"))
                .build();

        return Optional.of(discrepancyReportRepository.save(report).getReportId());
    }

    private String normalizeText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
