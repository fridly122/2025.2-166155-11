package itss.group11.frontend.screens.orderReconciliation;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;

import itss.group11.dto.orderExecution.PartialOrderSelectionDTO;
import itss.group11.dto.orderExecution.PurchaseOrderResponseDTO;
import itss.group11.dto.orderExecution.ReceivedLineDTO;
import itss.group11.dto.orderExecution.ReconciliationDetailDTO;
import itss.group11.dto.orderExecution.ReconciliationResultDTO;
import itss.group11.dto.orderExecution.ReconciliationSubmitDTO;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Region;
import javafx.util.converter.IntegerStringConverter;

public class OrderReconciliationController {

    @FXML
    private TableView<PurchaseOrderRow> poTable;

    @FXML
    private TableColumn<PurchaseOrderRow, String> colOrderId;

    @FXML
    private TableColumn<PurchaseOrderRow, String> colRequestCode;

    @FXML
    private TableColumn<PurchaseOrderRow, String> colSite;

    @FXML
    private TableColumn<PurchaseOrderRow, String> colStatus;

    @FXML
    private TableColumn<PurchaseOrderRow, String> colCreatedDate;

    @FXML
    private TableView<LineRow> lineTable;

    @FXML
    private TableColumn<LineRow, String> colMerchandiseCode;

    @FXML
    private TableColumn<LineRow, String> colMerchandiseName;

    @FXML
    private TableColumn<LineRow, Integer> colOrderedQty;

    @FXML
    private TableColumn<LineRow, Integer> colReceivedQty;

    @FXML
    private TableColumn<LineRow, Integer> colDifferenceQty;

    @FXML
    private TableColumn<LineRow, String> colUnit;

    @FXML
    private Label lblSelectedPo;

    @FXML
    private TextField txtCreatedBy;

    @FXML
    private TextArea txtReason;

    @FXML
    private TextArea txtNote;

    @FXML
    private Button btnConfirm;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL = ApiConfig.baseUrl("/api/v1/purchase-orders");

    @FXML
    public void initialize() {
        setupPurchaseOrderTable();
        setupLineTable();

        poTable.setPlaceholder(new Label("Không có đơn hàng IN_TRANSIT chờ đối soát."));
        lineTable.setPlaceholder(new Label("Chọn một đơn hàng để xem chi tiết."));

        btnConfirm.disableProperty().bind(lineTable.itemsProperty().isNull());

        poTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, selected) -> handlePoSelected(selected));

        loadInTransitOrders();
    }

    @FXML
    private void handleRefresh() {
        clearDetail();
        loadInTransitOrders();
    }

    @FXML
    private void handleConfirmReconciliation() {
        PurchaseOrderRow selected = poTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn đơn hàng", "Vui lòng chọn một đơn hàng trước khi xác nhận nhập kho.");
            return;
        }

        if (lineTable.getItems() == null || lineTable.getItems().isEmpty()) {
            showWarning("Chưa có dòng hàng", "Đơn hàng chưa có dòng hàng để đối soát.");
            return;
        }

        try {
            ReconciliationSubmitDTO dto = new ReconciliationSubmitDTO(
                    lineTable.getItems()
                            .stream()
                            .map(line -> new ReceivedLineDTO(line.getLineId(), line.getReceivedQty()))
                            .toList(),
                    txtReason.getText(),
                    txtNote.getText(),
                    txtCreatedBy.getText()
            );

            String encodedOrderId = URLEncoder.encode(selected.getOrderId(), StandardCharsets.UTF_8);
            String requestBody = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + encodedOrderId + "/reconcile"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ReconciliationResultDTO result =
                        objectMapper.readValue(response.body(), ReconciliationResultDTO.class);

                showInfo("Xác nhận nhập kho thành công", buildSuccessMessage(result));
                clearDetail();
                loadInTransitOrders();
            } else {
                showError("Đối soát thất bại", response.body());
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API xác nhận nhập kho: " + e.getMessage());
        }
    }

    private void setupPurchaseOrderTable() {
        colOrderId.setCellValueFactory(data -> data.getValue().orderIdProperty());
        colRequestCode.setCellValueFactory(data -> data.getValue().requestCodeProperty());
        colSite.setCellValueFactory(data -> data.getValue().siteProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colCreatedDate.setCellValueFactory(data -> data.getValue().createdDateProperty());
    }

    private void setupLineTable() {
        lineTable.setEditable(true);

        colMerchandiseCode.setCellValueFactory(data -> data.getValue().merchandiseCodeProperty());
        colMerchandiseName.setCellValueFactory(data -> data.getValue().merchandiseNameProperty());
        colOrderedQty.setCellValueFactory(data -> data.getValue().orderedQtyProperty().asObject());
        colReceivedQty.setCellValueFactory(data -> data.getValue().receivedQtyProperty().asObject());
        colDifferenceQty.setCellValueFactory(data -> data.getValue().differenceQtyProperty().asObject());
        colUnit.setCellValueFactory(data -> data.getValue().unitProperty());

        colReceivedQty.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colReceivedQty.setOnEditCommit(event -> {
            Integer value = event.getNewValue();
            if (value == null || value < 0) {
                showWarning("Dữ liệu không hợp lệ", "Số lượng thực nhận phải lớn hơn hoặc bằng 0.");
                lineTable.refresh();
                return;
            }

            LineRow row = event.getRowValue();
            row.setReceivedQty(value);
            row.recalculateDifference();
            lineTable.refresh();
        });
    }

    private void loadInTransitOrders() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/in-transit"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                PurchaseOrderResponseDTO[] rows =
                        objectMapper.readValue(response.body(), PurchaseOrderResponseDTO[].class);

                poTable.setItems(FXCollections.observableArrayList(
                        Arrays.stream(rows)
                                .map(row -> new PurchaseOrderRow(
                                        row.getOrderId(),
                                        row.getRequestCode(),
                                        row.getSiteCode() + " - " + row.getSiteName(),
                                        row.getStatus(),
                                        row.getCreatedDate()
                                ))
                                .toList()
                ));
            } else {
                showError("Không thể tải danh sách PO", response.body());
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API danh sách PO: " + e.getMessage());
        }
    }

    private void handlePoSelected(PurchaseOrderRow selected) {
        if (selected == null) {
            clearDetail();
            return;
        }

        lblSelectedPo.setText("Đang chọn: " + selected.getOrderId() + " | " + selected.getSite());
        loadReconciliationDetail(selected.getOrderId());
    }

    private void loadReconciliationDetail(String orderId) {
        try {
            String encodedOrderId = URLEncoder.encode(orderId, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + encodedOrderId + "/reconciliation"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ReconciliationDetailDTO detail =
                        objectMapper.readValue(response.body(), ReconciliationDetailDTO.class);

                lineTable.setItems(FXCollections.observableArrayList(
                        detail.getLines()
                                .stream()
                                .map(this::toLineRow)
                                .toList()
                ));
            } else {
                showError("Không thể tải chi tiết đối soát", response.body());
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API chi tiết đối soát: " + e.getMessage());
        }
    }

    private LineRow toLineRow(PartialOrderSelectionDTO dto) {
        return new LineRow(
                dto.getLineId(),
                dto.getMerchandiseCode(),
                dto.getMerchandiseName(),
                dto.getOrderedQty(),
                dto.getReceivedQty() == null ? 0 : dto.getReceivedQty(),
                dto.getUnit()
        );
    }

    private void clearDetail() {
        lblSelectedPo.setText("Chưa chọn đơn hàng");
        lineTable.setItems(FXCollections.observableArrayList());
        txtReason.clear();
        txtNote.clear();
    }

    private String buildSuccessMessage(ReconciliationResultDTO result) {
        StringBuilder builder = new StringBuilder();
        builder.append(result.getMessage()).append("\n");
        builder.append("Mã PO: ").append(result.getOrderId()).append("\n");
        builder.append("Trạng thái mới: ").append(result.getStatus()).append("\n");

        if (result.isHasDiscrepancy()
                && result.getDiscrepancyReportIds() != null
                && !result.getDiscrepancyReportIds().isEmpty()) {
            builder.append("Biên bản sai lệch:\n");
            for (String reportId : result.getDiscrepancyReportIds()) {
                builder.append("- ").append(reportId).append("\n");
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

    public static class PurchaseOrderRow {
        private final SimpleStringProperty orderId;
        private final SimpleStringProperty requestCode;
        private final SimpleStringProperty site;
        private final SimpleStringProperty status;
        private final SimpleStringProperty createdDate;

        public PurchaseOrderRow(String orderId, String requestCode, String site, String status, String createdDate) {
            this.orderId = new SimpleStringProperty(orderId);
            this.requestCode = new SimpleStringProperty(requestCode);
            this.site = new SimpleStringProperty(site);
            this.status = new SimpleStringProperty(status);
            this.createdDate = new SimpleStringProperty(createdDate);
        }

        public String getOrderId() {
            return orderId.get();
        }

        public String getSite() {
            return site.get();
        }

        public SimpleStringProperty orderIdProperty() {
            return orderId;
        }

        public SimpleStringProperty requestCodeProperty() {
            return requestCode;
        }

        public SimpleStringProperty siteProperty() {
            return site;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        public SimpleStringProperty createdDateProperty() {
            return createdDate;
        }
    }

    public static class LineRow {
        private final SimpleLongProperty lineId;
        private final SimpleStringProperty merchandiseCode;
        private final SimpleStringProperty merchandiseName;
        private final SimpleIntegerProperty orderedQty;
        private final SimpleIntegerProperty receivedQty;
        private final SimpleIntegerProperty differenceQty;
        private final SimpleStringProperty unit;

        public LineRow(
                Long lineId,
                String merchandiseCode,
                String merchandiseName,
                Integer orderedQty,
                Integer receivedQty,
                String unit
        ) {
            this.lineId = new SimpleLongProperty(lineId == null ? 0L : lineId);
            this.merchandiseCode = new SimpleStringProperty(merchandiseCode);
            this.merchandiseName = new SimpleStringProperty(merchandiseName);
            this.orderedQty = new SimpleIntegerProperty(orderedQty == null ? 0 : orderedQty);
            this.receivedQty = new SimpleIntegerProperty(receivedQty == null ? 0 : receivedQty);
            this.differenceQty = new SimpleIntegerProperty(this.orderedQty.get() - this.receivedQty.get());
            this.unit = new SimpleStringProperty(unit == null ? "" : unit);
        }

        public Long getLineId() {
            return lineId.get();
        }

        public Integer getReceivedQty() {
            return receivedQty.get();
        }

        public void setReceivedQty(Integer value) {
            receivedQty.set(value == null ? 0 : value);
        }

        public void recalculateDifference() {
            differenceQty.set(orderedQty.get() - receivedQty.get());
        }

        public SimpleStringProperty merchandiseCodeProperty() {
            return merchandiseCode;
        }

        public SimpleStringProperty merchandiseNameProperty() {
            return merchandiseName;
        }

        public SimpleIntegerProperty orderedQtyProperty() {
            return orderedQty;
        }

        public SimpleIntegerProperty receivedQtyProperty() {
            return receivedQty;
        }

        public SimpleIntegerProperty differenceQtyProperty() {
            return differenceQty;
        }

        public SimpleStringProperty unitProperty() {
            return unit;
        }
    }
}
