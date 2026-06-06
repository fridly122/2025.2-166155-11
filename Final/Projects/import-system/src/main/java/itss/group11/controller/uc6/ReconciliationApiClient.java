package itss.group11.controller.uc6;

import java.io.IOException;
import java.util.List;

import itss.group11.entity.uc6.PurchaseOrderResponseDTO;
import itss.group11.entity.uc6.ReconciliationDetailDTO;
import itss.group11.entity.uc6.ReconciliationResultDTO;
import itss.group11.entity.uc6.ReconciliationSubmitDTO;

public interface ReconciliationApiClient {

    List<PurchaseOrderResponseDTO> getInTransitOrders() throws IOException, InterruptedException;

    List<PurchaseOrderResponseDTO> getReceivedOrders() throws IOException, InterruptedException;

    ReconciliationDetailDTO getReconciliationDetail(String orderId) throws IOException, InterruptedException;

    ReconciliationResultDTO reconcile(String orderId, ReconciliationSubmitDTO dto) throws IOException, InterruptedException;

    ReconciliationResultDTO updateReceivedOrder(String orderId, ReconciliationSubmitDTO dto)
            throws IOException, InterruptedException;
}
