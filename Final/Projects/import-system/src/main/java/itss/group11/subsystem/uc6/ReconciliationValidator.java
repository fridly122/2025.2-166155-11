package itss.group11.subsystem.uc6;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import itss.group11.entity.chung.PurchaseOrder;
import itss.group11.entity.chung.PurchaseOrderLine;
import itss.group11.entity.uc6.ReceivedLineDTO;
import itss.group11.entity.uc6.ReconciliationSubmitDTO;

@Component
public class ReconciliationValidator {

    public void requireInTransit(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getStatus() != PurchaseOrder.PurchaseOrderStatus.IN_TRANSIT) {
            throw new RuntimeException("Chi co the doi soat don hang o trang thai IN_TRANSIT.");
        }
    }

    public Map<Long, ReceivedLineDTO> toReceivedLineMap(ReconciliationSubmitDTO dto) {
        if (dto == null || dto.getLines() == null || dto.getLines().isEmpty()) {
            throw new RuntimeException("Du lieu doi soat khong duoc de trong.");
        }

        Map<Long, ReceivedLineDTO> receivedByLineId = new LinkedHashMap<>();
        for (ReceivedLineDTO line : dto.getLines()) {
            validateReceivedLine(line);

            if (receivedByLineId.put(line.getLineId(), line) != null) {
                throw new RuntimeException("Dong hang bi trung trong du lieu doi soat: " + line.getLineId());
            }
        }
        return receivedByLineId;
    }

    public ReceivedLineDTO requireReceivedLine(Map<Long, ReceivedLineDTO> receivedByLineId, PurchaseOrderLine line) {
        ReceivedLineDTO receivedLine = receivedByLineId.get(line.getId());
        if (receivedLine == null) {
            throw new RuntimeException("Chua nhap so luong thuc nhan cho dong hang ID: " + line.getId());
        }
        return receivedLine;
    }

    public void requireOnlyExistingLines(Map<Long, ReceivedLineDTO> receivedByLineId, List<PurchaseOrderLine> lines) {
        Set<Long> orderLineIds = lines.stream()
                .map(PurchaseOrderLine::getId)
                .collect(Collectors.toSet());

        for (Long receivedLineId : receivedByLineId.keySet()) {
            if (!orderLineIds.contains(receivedLineId)) {
                throw new RuntimeException("Dong hang khong thuoc don dat hang dang doi soat: " + receivedLineId);
            }
        }
    }

    private void validateReceivedLine(ReceivedLineDTO line) {
        if (line == null || line.getLineId() == null) {
            throw new RuntimeException("Dong hang doi soat khong hop le.");
        }

        if (line.getReceivedQty() == null || line.getReceivedQty() < 0) {
            throw new RuntimeException("So luong thuc nhan khong hop le cho dong hang ID: " + line.getLineId());
        }
    }
}
