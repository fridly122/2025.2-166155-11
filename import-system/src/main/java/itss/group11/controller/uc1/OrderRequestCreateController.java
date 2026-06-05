package itss.group11.controller.uc1;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import itss.group11.entity.uc1.MerchandiseOptionDTO;
import itss.group11.entity.uc1.OrderRequestCreationDTO;
import itss.group11.entity.uc1.OrderRequestDetailDTO;
import itss.group11.entity.uc1.OrderRequestSummaryDTO;
import itss.group11.controller.chung.ApiConfig;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;

public class OrderRequestCreateController {

    @FXML
    private TableView<RequestSummaryRow> requestTable;

    @FXML
    private TableColumn<RequestSummaryRow, String> colRequestCode;

    @FXML
    private TableColumn<RequestSummaryRow, String> colStatus;

    @FXML
    private TableColumn<RequestSummaryRow, String> colDesiredDate;

    @FXML
    private TableColumn<RequestSummaryRow, String> colCreatedDate;

    @FXML
    private TableColumn<RequestSummaryRow, Integer> colItemCount;

    @FXML
    private DatePicker dpDesiredDeliveryDate;

    @FXML
    private ComboBox<MerchandiseOptionDTO> cboMerchandise;

    @FXML
    private Spinner<Integer> spnQuantity;

    @FXML
    private TableView<RequestItemRow> itemTable;

    @FXML
    private TableColumn<RequestItemRow, String> colItemCode;

    @FXML
    private TableColumn<RequestItemRow, String> colItemName;

    @FXML
    private TableColumn<RequestItemRow, Integer> colItemQuantity;

    @FXML
    private TableColumn<RequestItemRow, String> colItemUnit;

    @FXML
    private Button btnRemoveItem;

    @FXML
    private TextField txtSearchCode;

    @FXML
    private Label lblEditMode;

    @FXML
    private Button btnCreateRequest;

    @FXML
    private Button btnUpdateRequest;

    @FXML
    private Button btnLoadRequest;

    @FXML
    private Button btnDeleteRequest;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObservableList<RequestItemRow> requestItems = FXCollections.observableArrayList();

    private static final String BASE_URL = ApiConfig.baseUrl("/api/v1/order-requests");
    private static final String PENDING_STATUS = "PENDING";

    private String editingRequestCode;

    @FXML
    public void initialize() {
        setupRequestTable();
        setupItemTable();
        setupMerchandiseComboBox();

        requestTable.setPlaceholder(new Label("Chưa có yêu cầu nhập hàng."));
        itemTable.setPlaceholder(new Label("Chưa có mặt hàng nào trong yêu cầu."));
        itemTable.setItems(requestItems);

        btnRemoveItem.disableProperty().bind(itemTable.getSelectionModel().selectedItemProperty().isNull());
        requestTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, newRow) -> updateActionButtons());

        loadMerchandiseOptions();
        loadRequests(null);
        updateEditModeLabel();
        updateActionButtons();
    }

    @FXML
    private void handleRefresh() {
        loadMerchandiseOptions();
        loadRequests(txtSearchCode.getText());
    }

    @FXML
    private void handleSearch() {
        loadRequests(txtSearchCode.getText());
    }

    @FXML
    private void handleShowAll() {
        txtSearchCode.clear();
        loadRequests(null);
    }

    @FXML
    private void handleLoadSelectedRequest() {
        RequestSummaryRow selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn yêu cầu", "Vui lòng chọn một yêu cầu trong bảng.");
            return;
        }

        if (!PENDING_STATUS.equalsIgnoreCase(selected.getStatus())) {
            showWarning(
                    "Không thể sửa",
                    "Chỉ được sửa yêu cầu ở trạng thái PENDING. Mã: " + selected.getRequestCode()
            );
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + encodePath(selected.getRequestCode())))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                showError("Không thể tải chi tiết yêu cầu", response.body());
                return;
            }

            OrderRequestDetailDTO detail = objectMapper.readValue(response.body(), OrderRequestDetailDTO.class);
            populateFormFromDetail(detail);
            editingRequestCode = detail.getRequestCode();
            updateEditModeLabel();
            updateActionButtons();
        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API chi tiết yêu cầu: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API chi tiết yêu cầu: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateRequest() {
        if (editingRequestCode == null || editingRequestCode.isBlank()) {
            showWarning("Chưa chọn yêu cầu", "Hãy chọn yêu cầu PENDING và bấm \"Nạp vào form\" trước khi cập nhật.");
            return;
        }

        Optional<OrderRequestDetailDTO> updated = submitRequest(
                URI.create(BASE_URL + "/" + encodePath(editingRequestCode)),
                true,
                "Cập nhật yêu cầu nhập hàng thất bại"
        );

        updated.ifPresent(detail -> {
            showInfo(
                    "Cập nhật thành công",
                    "Mã yêu cầu: " + detail.getRequestCode()
                            + "\nTrạng thái: " + detail.getStatus()
                            + "\nSố mặt hàng: " + detail.getItems().size()
            );
            clearForm();
            loadRequests(txtSearchCode.getText());
        });
    }

    @FXML
    private void handleDeleteRequest() {
        RequestSummaryRow selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn yêu cầu", "Vui lòng chọn yêu cầu cần xóa.");
            return;
        }

        if (!PENDING_STATUS.equalsIgnoreCase(selected.getStatus())) {
            showWarning(
                    "Không thể hủy/xóa",
                    "Chỉ được hủy hoặc xóa yêu cầu ở trạng thái PENDING. Mã: " + selected.getRequestCode()
            );
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy/xóa");
        confirm.setHeaderText("Hủy hoặc xóa yêu cầu " + selected.getRequestCode() + "?");
        confirm.setContentText(
                "Nếu yêu cầu chưa có xử lý liên quan, hệ thống sẽ xóa khỏi danh sách.\n"
                        + "Nếu đã có phiếu hỏi tồn kho, yêu cầu sẽ được chuyển sang trạng thái CANCELLED."
        );
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.OK) {
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + encodePath(selected.getRequestCode())))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                if (selected.getRequestCode().equalsIgnoreCase(editingRequestCode)) {
                    clearForm();
                }
                showInfo("Thành công", response.body());
                loadRequests(txtSearchCode.getText());
            } else {
                showError("Hủy/xóa yêu cầu thất bại", response.body());
            }
        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API hủy/xóa yêu cầu: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API hủy/xóa yêu cầu: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddItem() {
        MerchandiseOptionDTO selectedMerchandise = cboMerchandise.getSelectionModel().getSelectedItem();
        Integer quantity = spnQuantity.getValue();

        if (selectedMerchandise == null) {
            showWarning("Chưa chọn mặt hàng", "Vui lòng chọn một mặt hàng trước khi thêm vào yêu cầu.");
            return;
        }

        if (quantity == null || quantity <= 0) {
            showWarning("Số lượng không hợp lệ", "Số lượng đặt phải lớn hơn 0.");
            return;
        }

        for (RequestItemRow row : requestItems) {
            if (row.getMerchandiseCode().equals(selectedMerchandise.getCode())) {
                row.increaseQuantity(quantity);
                itemTable.refresh();
                return;
            }
        }

        requestItems.add(new RequestItemRow(
                selectedMerchandise.getCode(),
                selectedMerchandise.getName(),
                quantity,
                selectedMerchandise.getUnit()
        ));
    }

    @FXML
    private void handleRemoveItem() {
        RequestItemRow selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            requestItems.remove(selected);
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    @FXML
    private void handleCreateRequest() {
        Optional<OrderRequestDetailDTO> created = submitRequest(
                URI.create(BASE_URL),
                false,
                "Tạo yêu cầu nhập hàng thất bại"
        );

        created.ifPresent(detail -> {
            showInfo(
                    "Tạo yêu cầu nhập hàng thành công",
                    "Mã yêu cầu: " + detail.getRequestCode()
                            + "\nTrạng thái: " + detail.getStatus()
                            + "\nSố mặt hàng: " + detail.getItems().size()
            );
            clearForm();
            loadRequests(txtSearchCode.getText());
        });
    }

    private void setupRequestTable() {
        colRequestCode.setCellValueFactory(data -> data.getValue().requestCodeProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colDesiredDate.setCellValueFactory(data -> data.getValue().desiredDateProperty());
        colCreatedDate.setCellValueFactory(data -> data.getValue().createdDateProperty());
        colItemCount.setCellValueFactory(data -> data.getValue().itemCountProperty().asObject());
    }

    private void setupItemTable() {
        colItemCode.setCellValueFactory(data -> data.getValue().merchandiseCodeProperty());
        colItemName.setCellValueFactory(data -> data.getValue().merchandiseNameProperty());
        colItemQuantity.setCellValueFactory(data -> data.getValue().quantityProperty().asObject());
        colItemUnit.setCellValueFactory(data -> data.getValue().unitProperty());
    }

    private void setupMerchandiseComboBox() {
        cboMerchandise.setConverter(new StringConverter<>() {
            @Override
            public String toString(MerchandiseOptionDTO merchandise) {
                if (merchandise == null) {
                    return "";
                }

                return merchandise.getCode() + " - " + merchandise.getName();
            }

            @Override
            public MerchandiseOptionDTO fromString(String value) {
                return null;
            }
        });
    }

    private void loadMerchandiseOptions() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/merchandise"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                MerchandiseOptionDTO[] merchandise =
                        objectMapper.readValue(response.body(), MerchandiseOptionDTO[].class);

                cboMerchandise.setItems(FXCollections.observableArrayList(merchandise));
            } else {
                showError("Không thể tải danh sách mặt hàng", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API danh sách mặt hàng: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API danh sách mặt hàng: " + e.getMessage());
        }
    }

    private void loadRequests(String requestCode) {
        try {
            String uri = BASE_URL;
            if (requestCode != null && !requestCode.isBlank()) {
                uri = BASE_URL + "?requestCode=" + URLEncoder.encode(requestCode.trim(), StandardCharsets.UTF_8);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                OrderRequestSummaryDTO[] rows =
                        objectMapper.readValue(response.body(), OrderRequestSummaryDTO[].class);

                requestTable.setItems(FXCollections.observableArrayList(
                        Arrays.stream(rows)
                                .map(row -> new RequestSummaryRow(
                                        row.getRequestCode(),
                                        row.getStatus(),
                                        row.getDesiredDeliveryDate(),
                                        row.getCreatedDate(),
                                        row.getItemCount()
                                ))
                                .toList()
                ));
            } else {
                showError("Không thể tải danh sách yêu cầu", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API danh sách yêu cầu nhập hàng: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API danh sách yêu cầu nhập hàng: " + e.getMessage());
        }
    }

    private Optional<OrderRequestDetailDTO> submitRequest(URI uri, boolean update, String errorTitle) {
        if (dpDesiredDeliveryDate.getValue() == null) {
            showWarning("Thiếu ngày nhận hàng", "Vui lòng chọn ngày mong muốn nhận hàng.");
            return Optional.empty();
        }

        if (requestItems.isEmpty()) {
            showWarning("Chưa có mặt hàng", "Yêu cầu nhập hàng phải có ít nhất một mặt hàng.");
            return Optional.empty();
        }

        try {
            String body = buildRequestBody();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json");

            HttpRequest httpRequest = update
                    ? requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body)).build()
                    : requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body)).build();

            HttpResponse<String> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return Optional.of(objectMapper.readValue(response.body(), OrderRequestDetailDTO.class));
            }

            showError(errorTitle, response.body());
            return Optional.empty();
        } catch (IOException e) {
            showError("Lỗi kết nối", errorTitle + ": " + e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", errorTitle + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private String buildRequestBody() throws IOException {
        OrderRequestCreationDTO dto = new OrderRequestCreationDTO(
                editingRequestCode,
                dpDesiredDeliveryDate.getValue().toString(),
                buildItemDTOs()
        );
        return objectMapper.writeValueAsString(dto);
    }

    private void populateFormFromDetail(OrderRequestDetailDTO detail) {
        requestItems.clear();

        if (detail.getDesiredDeliveryDate() != null && !detail.getDesiredDeliveryDate().isBlank()) {
            dpDesiredDeliveryDate.setValue(LocalDate.parse(detail.getDesiredDeliveryDate()));
        } else {
            dpDesiredDeliveryDate.setValue(null);
        }

        if (detail.getItems() != null) {
            for (OrderRequestDetailDTO.ItemDTO item : detail.getItems()) {
                requestItems.add(new RequestItemRow(
                        item.getMerchandiseCode(),
                        item.getMerchandiseName(),
                        item.getQuantityOrdered(),
                        item.getUnit()
                ));
            }
        }

        cboMerchandise.getSelectionModel().clearSelection();
        spnQuantity.getValueFactory().setValue(1);
    }

    private void updateEditModeLabel() {
        if (editingRequestCode == null || editingRequestCode.isBlank()) {
            lblEditMode.setText("");
        } else {
            lblEditMode.setText("Đang sửa yêu cầu: " + editingRequestCode);
        }
    }

    private void updateActionButtons() {
        boolean editing = editingRequestCode != null && !editingRequestCode.isBlank();
        btnCreateRequest.setDisable(editing);
        btnUpdateRequest.setDisable(!editing);

        RequestSummaryRow selected = requestTable.getSelectionModel().getSelectedItem();
        boolean pendingSelected = selected != null && PENDING_STATUS.equalsIgnoreCase(selected.getStatus());
        btnDeleteRequest.setDisable(!pendingSelected);
        btnLoadRequest.setDisable(selected == null);
    }

    private String encodePath(String requestCode) {
        return URLEncoder.encode(requestCode, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private List<OrderRequestCreationDTO.ItemDTO> buildItemDTOs() {
        return requestItems.stream()
                .map(row -> new OrderRequestCreationDTO.ItemDTO(
                        row.getMerchandiseCode(),
                        row.getQuantity()
                ))
                .toList();
    }

    private void clearForm() {
        editingRequestCode = null;
        dpDesiredDeliveryDate.setValue(null);
        cboMerchandise.getSelectionModel().clearSelection();
        spnQuantity.getValueFactory().setValue(1);
        requestItems.clear();
        requestTable.getSelectionModel().clearSelection();
        updateEditModeLabel();
        updateActionButtons();
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

    public static class RequestSummaryRow {
        private final SimpleStringProperty requestCode;
        private final SimpleStringProperty status;
        private final SimpleStringProperty desiredDate;
        private final SimpleStringProperty createdDate;
        private final SimpleIntegerProperty itemCount;

        public RequestSummaryRow(
                String requestCode,
                String status,
                String desiredDate,
                String createdDate,
                Integer itemCount
        ) {
            this.requestCode = new SimpleStringProperty(requestCode == null ? "" : requestCode);
            this.status = new SimpleStringProperty(status == null ? "" : status);
            this.desiredDate = new SimpleStringProperty(desiredDate == null ? "" : desiredDate);
            this.createdDate = new SimpleStringProperty(createdDate == null ? "" : createdDate);
            this.itemCount = new SimpleIntegerProperty(itemCount == null ? 0 : itemCount);
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

        public SimpleStringProperty createdDateProperty() {
            return createdDate;
        }

        public SimpleIntegerProperty itemCountProperty() {
            return itemCount;
        }

        public String getRequestCode() {
            return requestCode.get();
        }

        public String getStatus() {
            return status.get();
        }
    }

    public static class RequestItemRow {
        private final SimpleStringProperty merchandiseCode;
        private final SimpleStringProperty merchandiseName;
        private final SimpleIntegerProperty quantity;
        private final SimpleStringProperty unit;

        public RequestItemRow(String merchandiseCode, String merchandiseName, Integer quantity, String unit) {
            this.merchandiseCode = new SimpleStringProperty(merchandiseCode == null ? "" : merchandiseCode);
            this.merchandiseName = new SimpleStringProperty(merchandiseName == null ? "" : merchandiseName);
            this.quantity = new SimpleIntegerProperty(quantity == null ? 0 : quantity);
            this.unit = new SimpleStringProperty(unit == null ? "" : unit);
        }

        public String getMerchandiseCode() {
            return merchandiseCode.get();
        }

        public Integer getQuantity() {
            return quantity.get();
        }

        public void increaseQuantity(Integer value) {
            quantity.set(quantity.get() + (value == null ? 0 : value));
        }

        public SimpleStringProperty merchandiseCodeProperty() {
            return merchandiseCode;
        }

        public SimpleStringProperty merchandiseNameProperty() {
            return merchandiseName;
        }

        public SimpleIntegerProperty quantityProperty() {
            return quantity;
        }

        public SimpleStringProperty unitProperty() {
            return unit;
        }
    }
}

