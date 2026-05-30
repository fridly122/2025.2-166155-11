package itss.group11.frontend.screens.siteClassification;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;

import itss.group11.dto.requestManage.OrderRequestDetailDTO;
import itss.group11.dto.requestManage.OrderRequestSummaryDTO;
import itss.group11.dto.siteSync.InventoryInquirySendResultDTO;
import itss.group11.dto.siteSync.OrderRequestClassificationDTO;
import itss.group11.dto.siteSync.SiteClassificationResultDTO;
import itss.group11.frontend.ApiConfig;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;

public class SiteClassificationController {

    @FXML
    private TableView<RequestRow> requestTable;

    @FXML
    private TableColumn<RequestRow, String> colRequestCode;

    @FXML
    private TableColumn<RequestRow, String> colRequestStatus;

    @FXML
    private TableColumn<RequestRow, String> colDesiredDate;

    @FXML
    private TableColumn<RequestRow, Integer> colItemCount;

    @FXML
    private TableView<RequestItemRow> itemTable;

    @FXML
    private TableColumn<RequestItemRow, String> colItemCode;

    @FXML
    private TableColumn<RequestItemRow, String> colItemName;

    @FXML
    private TableColumn<RequestItemRow, Integer> colRequiredQty;

    @FXML
    private TableColumn<RequestItemRow, String> colUnit;

    @FXML
    private Button btnClassify;

    @FXML
    private Button btnSendInquiry;

    @FXML
    private Label lblSummary;

    @FXML
    private TableView<SiteResultRow> resultTable;

    @FXML
    private TableColumn<SiteResultRow, String> colSiteCode;

    @FXML
    private TableColumn<SiteResultRow, String> colSiteName;

    @FXML
    private TableColumn<SiteResultRow, String> colResultItemCode;

    @FXML
    private TableColumn<SiteResultRow, String> colResultItemName;

    @FXML
    private TableColumn<SiteResultRow, Integer> colResultRequiredQty;

    @FXML
    private TableColumn<SiteResultRow, Integer> colInStock;

    @FXML
    private TableColumn<SiteResultRow, String> colClassification;

    @FXML
    private TableColumn<SiteResultRow, String> colTransport;

    @FXML
    private TableColumn<SiteResultRow, Integer> colEstimatedDays;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL = ApiConfig.baseUrl("/api/v1/site-sync");

    @FXML
    public void initialize() {
        setupRequestTable();
        setupItemTable();
        setupResultTable();

        requestTable.setPlaceholder(new Label("Không có yêu cầu PENDING cần phân loại."));
        itemTable.setPlaceholder(new Label("Chọn một yêu cầu để xem danh sách mặt hàng."));
        resultTable.setPlaceholder(new Label("Bấm Bắt đầu phân loại để xem kết quả."));
        lblSummary.setText("Chưa có kết quả phân loại.");

        btnClassify.disableProperty().bind(requestTable.getSelectionModel().selectedItemProperty().isNull());
        btnSendInquiry.disableProperty().bind(resultTable.itemsProperty().isNull());

        requestTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, selected) -> handleRequestSelected(selected));

        loadPendingRequests();
    }

    @FXML
    private void handleRefresh() {
        clearDetails();
        loadPendingRequests();
    }

    @FXML
    private void handleClassify() {
        RequestRow selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn yêu cầu", "Vui lòng chọn một yêu cầu nhập hàng trước khi phân loại.");
            return;
        }

        try {
            String encodedRequestCode = encode(selected.getRequestCode());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/requests/" + encodedRequestCode + "/classify"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                OrderRequestClassificationDTO result =
                        objectMapper.readValue(response.body(), OrderRequestClassificationDTO.class);

                renderClassificationResult(result);
            } else {
                showError("Phân loại thất bại", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API phân loại: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API phân loại: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendInquiry() {
        RequestRow selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn yêu cầu", "Vui lòng chọn một yêu cầu nhập hàng trước khi gửi hỏi tồn kho.");
            return;
        }

        try {
            String encodedRequestCode = encode(selected.getRequestCode());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/requests/" + encodedRequestCode + "/send-inventory-inquiry"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                InventoryInquirySendResultDTO result =
                        objectMapper.readValue(response.body(), InventoryInquirySendResultDTO.class);

                showInfo("Gửi yêu cầu hỏi tồn kho thành công", result.getMessage());
            } else {
                showError("Gửi yêu cầu hỏi tồn kho thất bại", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API gửi hỏi tồn kho: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API gửi hỏi tồn kho: " + e.getMessage());
        }
    }

    private void setupRequestTable() {
        colRequestCode.setCellValueFactory(data -> data.getValue().requestCodeProperty());
        colRequestStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colDesiredDate.setCellValueFactory(data -> data.getValue().desiredDateProperty());
        colItemCount.setCellValueFactory(data -> data.getValue().itemCountProperty().asObject());
    }

    private void setupItemTable() {
        colItemCode.setCellValueFactory(data -> data.getValue().merchandiseCodeProperty());
        colItemName.setCellValueFactory(data -> data.getValue().merchandiseNameProperty());
        colRequiredQty.setCellValueFactory(data -> data.getValue().requiredQtyProperty().asObject());
        colUnit.setCellValueFactory(data -> data.getValue().unitProperty());
    }

    private void setupResultTable() {
        colSiteCode.setCellValueFactory(data -> data.getValue().siteCodeProperty());
        colSiteName.setCellValueFactory(data -> data.getValue().siteNameProperty());
        colResultItemCode.setCellValueFactory(data -> data.getValue().merchandiseCodeProperty());
        colResultItemName.setCellValueFactory(data -> data.getValue().merchandiseNameProperty());
        colResultRequiredQty.setCellValueFactory(data -> data.getValue().requiredQtyProperty().asObject());
        colInStock.setCellValueFactory(data -> data.getValue().inStockQuantityProperty().asObject());
        colClassification.setCellValueFactory(data -> data.getValue().classificationProperty());
        colTransport.setCellValueFactory(data -> data.getValue().transportProperty());
        colEstimatedDays.setCellValueFactory(data -> data.getValue().estimatedDaysProperty().asObject());
    }

    private void loadPendingRequests() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/pending-requests"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                OrderRequestSummaryDTO[] rows =
                        objectMapper.readValue(response.body(), OrderRequestSummaryDTO[].class);

                requestTable.setItems(FXCollections.observableArrayList(
                        Arrays.stream(rows)
                                .map(row -> new RequestRow(
                                        row.getRequestCode(),
                                        row.getStatus(),
                                        row.getDesiredDeliveryDate(),
                                        row.getItemCount()
                                ))
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

    private void handleRequestSelected(RequestRow selected) {
        resultTable.setItems(FXCollections.observableArrayList());
        lblSummary.setText("Chưa có kết quả phân loại.");

        if (selected == null) {
            itemTable.setItems(FXCollections.observableArrayList());
            return;
        }

        loadRequestDetail(selected.getRequestCode());
    }

    private void loadRequestDetail(String requestCode) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/requests/" + encode(requestCode)))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                OrderRequestDetailDTO detail =
                        objectMapper.readValue(response.body(), OrderRequestDetailDTO.class);

                itemTable.setItems(FXCollections.observableArrayList(
                        detail.getItems() == null
                                ? java.util.List.of()
                                : detail.getItems().stream().map(this::toRequestItemRow).toList()
                ));
            } else {
                showError("Không thể tải chi tiết yêu cầu", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API chi tiết yêu cầu: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API chi tiết yêu cầu: " + e.getMessage());
        }
    }

    private void renderClassificationResult(OrderRequestClassificationDTO result) {
        lblSummary.setText(result.getMessage());
        resultTable.setItems(FXCollections.observableArrayList(
                result.getResults() == null
                        ? java.util.List.of()
                        : result.getResults().stream().map(this::toSiteResultRow).toList()
        ));
    }

    private RequestItemRow toRequestItemRow(OrderRequestDetailDTO.ItemDTO dto) {
        return new RequestItemRow(
                dto.getId(),
                dto.getMerchandiseCode(),
                dto.getMerchandiseName(),
                dto.getQuantityOrdered(),
                dto.getUnit()
        );
    }

    private SiteResultRow toSiteResultRow(SiteClassificationResultDTO dto) {
        return new SiteResultRow(
                dto.getSiteCode(),
                dto.getSiteName(),
                dto.getMerchandiseCode(),
                dto.getMerchandiseName(),
                dto.getRequiredQuantity(),
                dto.getInStockQuantity(),
                dto.getClassification(),
                dto.getSuggestedTransportMeans(),
                dto.getEstimatedDeliveryDays()
        );
    }

    private void clearDetails() {
        itemTable.setItems(FXCollections.observableArrayList());
        resultTable.setItems(FXCollections.observableArrayList());
        lblSummary.setText("Chưa có kết quả phân loại.");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
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
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
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
        private final SimpleStringProperty requestCode;
        private final SimpleStringProperty status;
        private final SimpleStringProperty desiredDate;
        private final SimpleIntegerProperty itemCount;

        public RequestRow(String requestCode, String status, String desiredDate, Integer itemCount) {
            this.requestCode = new SimpleStringProperty(requestCode == null ? "" : requestCode);
            this.status = new SimpleStringProperty(status == null ? "" : status);
            this.desiredDate = new SimpleStringProperty(desiredDate == null ? "" : desiredDate);
            this.itemCount = new SimpleIntegerProperty(itemCount == null ? 0 : itemCount);
        }

        public String getRequestCode() {
            return requestCode.get();
        }

        public SimpleStringProperty requestCodeProperty() {
            return requestCode;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        public SimpleStringProperty desiredDateProperty() {
            return desiredDate;
        }

        public SimpleIntegerProperty itemCountProperty() {
            return itemCount;
        }
    }

    public static class RequestItemRow {
        private final SimpleLongProperty id;
        private final SimpleStringProperty merchandiseCode;
        private final SimpleStringProperty merchandiseName;
        private final SimpleIntegerProperty requiredQty;
        private final SimpleStringProperty unit;

        public RequestItemRow(Long id, String merchandiseCode, String merchandiseName, Integer requiredQty, String unit) {
            this.id = new SimpleLongProperty(id == null ? 0L : id);
            this.merchandiseCode = new SimpleStringProperty(merchandiseCode == null ? "" : merchandiseCode);
            this.merchandiseName = new SimpleStringProperty(merchandiseName == null ? "" : merchandiseName);
            this.requiredQty = new SimpleIntegerProperty(requiredQty == null ? 0 : requiredQty);
            this.unit = new SimpleStringProperty(unit == null ? "" : unit);
        }

        public SimpleStringProperty merchandiseCodeProperty() {
            return merchandiseCode;
        }

        public SimpleStringProperty merchandiseNameProperty() {
            return merchandiseName;
        }

        public SimpleIntegerProperty requiredQtyProperty() {
            return requiredQty;
        }

        public SimpleStringProperty unitProperty() {
            return unit;
        }
    }

    public static class SiteResultRow {
        private final SimpleStringProperty siteCode;
        private final SimpleStringProperty siteName;
        private final SimpleStringProperty merchandiseCode;
        private final SimpleStringProperty merchandiseName;
        private final SimpleIntegerProperty requiredQty;
        private final SimpleIntegerProperty inStockQuantity;
        private final SimpleStringProperty classification;
        private final SimpleStringProperty transport;
        private final SimpleIntegerProperty estimatedDays;

        public SiteResultRow(
                String siteCode,
                String siteName,
                String merchandiseCode,
                String merchandiseName,
                Integer requiredQty,
                Integer inStockQuantity,
                String classification,
                String transport,
                Integer estimatedDays
        ) {
            this.siteCode = new SimpleStringProperty(siteCode == null ? "" : siteCode);
            this.siteName = new SimpleStringProperty(siteName == null ? "" : siteName);
            this.merchandiseCode = new SimpleStringProperty(merchandiseCode == null ? "" : merchandiseCode);
            this.merchandiseName = new SimpleStringProperty(merchandiseName == null ? "" : merchandiseName);
            this.requiredQty = new SimpleIntegerProperty(requiredQty == null ? 0 : requiredQty);
            this.inStockQuantity = new SimpleIntegerProperty(inStockQuantity == null ? 0 : inStockQuantity);
            this.classification = new SimpleStringProperty(classification == null ? "" : classification);
            this.transport = new SimpleStringProperty(transport == null ? "" : transport);
            this.estimatedDays = new SimpleIntegerProperty(estimatedDays == null ? 0 : estimatedDays);
        }

        public SimpleStringProperty siteCodeProperty() {
            return siteCode;
        }

        public SimpleStringProperty siteNameProperty() {
            return siteName;
        }

        public SimpleStringProperty merchandiseCodeProperty() {
            return merchandiseCode;
        }

        public SimpleStringProperty merchandiseNameProperty() {
            return merchandiseName;
        }

        public SimpleIntegerProperty requiredQtyProperty() {
            return requiredQty;
        }

        public SimpleIntegerProperty inStockQuantityProperty() {
            return inStockQuantity;
        }

        public SimpleStringProperty classificationProperty() {
            return classification;
        }

        public SimpleStringProperty transportProperty() {
            return transport;
        }

        public SimpleIntegerProperty estimatedDaysProperty() {
            return estimatedDays;
        }
    }
}
