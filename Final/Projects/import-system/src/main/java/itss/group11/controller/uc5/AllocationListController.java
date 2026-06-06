package itss.group11.controller.uc5;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;

import itss.group11.entity.uc5.AllocationInventorySummaryDTO;
import itss.group11.entity.uc5.AllocationPlanDTO;
import itss.group11.entity.uc5.AllocationPlanItemDTO;
import itss.group11.entity.uc5.AllocationRequestRowDTO;
import itss.group11.entity.uc5.AllocationResultDTO;
import itss.group11.controller.chung.ApiConfig;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;

public class AllocationListController {

    @FXML
    private TableView<RequestRow> requestTable;

    @FXML
    private TableColumn<RequestRow, String> colRequestCode;

    @FXML
    private TableColumn<RequestRow, String> colStatus;

    @FXML
    private TableColumn<RequestRow, String> colDate;

    @FXML
    private Button btnPreview;

    @FXML
    private Button btnProcess;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL = ApiConfig.baseUrl("/api/v1/allocations");

    @FXML
    public void initialize() {
        colRequestCode.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRequestCode()));

        colStatus.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus()));

        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCreatedDate()));

        requestTable.setPlaceholder(new Label("Chưa có yêu cầu PENDING chờ lập kế hoạch."));

        btnPreview.disableProperty().bind(requestTable.getSelectionModel().selectedItemProperty().isNull());
        btnProcess.disableProperty().bind(requestTable.getSelectionModel().selectedItemProperty().isNull());

        loadPendingRequests();
    }

    private void loadPendingRequests() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/pending"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                AllocationRequestRowDTO[] rows =
                        objectMapper.readValue(response.body(), AllocationRequestRowDTO[].class);

                requestTable.setItems(FXCollections.observableArrayList(
                        java.util.Arrays.stream(rows)
                                .map(row -> new RequestRow(
                                        row.getRequestCode(),
                                        row.getStatus(),
                                        row.getCreatedDate()))
                                .toList()
                ));
            } else {
                showError("Không thể tải danh sách yêu cầu", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API danh sách yêu cầu: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API danh sách yêu cầu: " + e.getMessage());
        }
    }

    @FXML
    private void handlePreview() {
        RequestRow selected = requestTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Chưa chọn yêu cầu", "Vui lòng chọn một yêu cầu nhập hàng trước khi xem trước kế hoạch.");
            return;
        }

        String requestCode = selected.getRequestCode();

        try {
            String encodedRequestCode = URLEncoder.encode(requestCode, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/preview/" + encodedRequestCode))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                AllocationPlanDTO plan = objectMapper.readValue(response.body(), AllocationPlanDTO.class);
                showPreviewDialog(plan);
            } else {
                showError("Không thể xem trước kế hoạch", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API preview: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API preview: " + e.getMessage());
        }
    }

    @FXML
    private void handleProcessAllocation() {
        RequestRow selected = requestTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Chưa chọn yêu cầu", "Vui lòng chọn một yêu cầu nhập hàng trước khi xử lý lập kế hoạch.");
            return;
        }

        String requestCode = selected.getRequestCode();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xử lý");
        confirm.setHeaderText("Xử lý lập kế hoạch cho yêu cầu: " + requestCode);
        confirm.setContentText("Sau khi xử lý, hệ thống sẽ tạo đơn PO và cập nhật trạng thái yêu cầu. Bạn có chắc muốn tiếp tục?");

        ButtonType result = confirm.showAndWait().orElse(ButtonType.CANCEL);

        if (result != ButtonType.OK) {
            return;
        }

        try {
            String encodedRequestCode = URLEncoder.encode(requestCode, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/process/" + encodedRequestCode))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                AllocationResultDTO allocationResult =
                        objectMapper.readValue(response.body(), AllocationResultDTO.class);

                showInfo("Xử lý thành công", buildProcessSuccessMessage(allocationResult));
                loadPendingRequests();
            } else {
                showError("Xử lý thất bại", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API process: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API process: " + e.getMessage());
        }
    }

    private void showPreviewDialog(AllocationPlanDTO plan) {
        StringBuilder builder = new StringBuilder();

        builder.append(plan.getMessage()).append("\n\n");
        builder.append("Mã yêu cầu: ").append(plan.getRequestCode()).append("\n");
        builder.append("Tình trạng tồn kho: ")
                .append(plan.isEnoughInventory() ? "Đủ hàng" : "Không đủ hàng")
                .append("\n\n");

        if (plan.getInventorySummaries() != null && !plan.getInventorySummaries().isEmpty()) {
            builder.append("Thông tin tồn kho thực tế:\n");

            for (AllocationInventorySummaryDTO summary : plan.getInventorySummaries()) {
                builder.append("- Mặt hàng: ")
                        .append(summary.getMerchandiseCode());

                if (summary.getMerchandiseName() != null && !summary.getMerchandiseName().isBlank()) {
                    builder.append(" - ").append(summary.getMerchandiseName());
                }

                builder.append(" | SL yêu cầu: ")
                        .append(summary.getRequestedQuantity())
                        .append(" | Tổng tồn kho thực tế: ")
                        .append(summary.getTotalInStockQuantity());

                if (summary.getShortageQuantity() > 0) {
                    builder.append(" | Thiếu: ")
                            .append(summary.getShortageQuantity());
                }

                builder.append("\n");
            }

            builder.append("\n");
        }

        if (plan.getPlanItems() == null || plan.getPlanItems().isEmpty()) {
            builder.append("Không có dòng phân bổ nào.");
        } else {
            builder.append("Danh sách phân bổ dự kiến:\n");

            for (AllocationPlanItemDTO item : plan.getPlanItems()) {
                builder.append("- Mặt hàng: ")
                        .append(item.getMerchandiseCode())
                        .append(" | Site: ")
                        .append(item.getSiteCode())
                        .append(" - ")
                        .append(item.getSiteName())
                        .append(" | SL yêu cầu: ")
                        .append(item.getRequestedQuantity())
                        .append(" | Tồn kho site: ")
                        .append(item.getInStockQuantity())
                        .append(" | SL phân bổ: ")
                        .append(item.getAllocatedQuantity())
                        .append(" | VC: ")
                        .append(item.getDeliveryMeans() == null ? "" : item.getDeliveryMeans())
                        .append("\n");
            }
        }

        showInfo("Xem trước kế hoạch", builder.toString());
    }

    private String buildProcessSuccessMessage(AllocationResultDTO result) {
        StringBuilder builder = new StringBuilder();

        builder.append(result.getMessage()).append("\n\n");
        builder.append("Mã yêu cầu: ").append(result.getRequestCode()).append("\n");

        if (result.getGeneratedPoCodes() != null && !result.getGeneratedPoCodes().isEmpty()) {
            builder.append("Các đơn PO đã tạo:\n");
            for (String poCode : result.getGeneratedPoCodes()) {
                builder.append("- ").append(poCode).append("\n");
            }
        }

        return builder.toString();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    public static class RequestRow {
        private final String requestCode;
        private String status;
        private final String createdDate;

        public RequestRow(String requestCode, String status, String createdDate) {
            this.requestCode = requestCode;
            this.status = status;
            this.createdDate = createdDate;
        }

        public String getRequestCode() {
            return requestCode;
        }

        public String getStatus() {
            return status;
        }

        public String getCreatedDate() {
            return createdDate;
        }
        public void setStatus(String status) {
            this.status = status;
        }
    }
}

