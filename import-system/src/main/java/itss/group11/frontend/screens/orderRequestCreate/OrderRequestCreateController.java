package itss.group11.frontend.screens.orderRequestCreate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import itss.group11.dto.requestManage.MerchandiseOptionDTO;
import itss.group11.dto.requestManage.OrderRequestCreationDTO;
import itss.group11.dto.requestManage.OrderRequestDetailDTO;
import itss.group11.dto.requestManage.OrderRequestSummaryDTO;
import itss.group11.frontend.ApiConfig;
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
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObservableList<RequestItemRow> requestItems = FXCollections.observableArrayList();

    private static final String BASE_URL = ApiConfig.baseUrl("/api/v1/order-requests");

    @FXML
    public void initialize() {
        setupRequestTable();
        setupItemTable();
        setupMerchandiseComboBox();

        requestTable.setPlaceholder(new Label("Chưa có yêu cầu nhập hàng."));
        itemTable.setPlaceholder(new Label("Chưa có mặt hàng nào trong yêu cầu."));
        itemTable.setItems(requestItems);

        btnRemoveItem.disableProperty().bind(itemTable.getSelectionModel().selectedItemProperty().isNull());

        loadMerchandiseOptions();
        loadRequests();
    }

    @FXML
    private void handleRefresh() {
        loadMerchandiseOptions();
        loadRequests();
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
        if (dpDesiredDeliveryDate.getValue() == null) {
            showWarning("Thiếu ngày nhận hàng", "Vui lòng chọn ngày mong muốn nhận hàng.");
            return;
        }

        if (requestItems.isEmpty()) {
            showWarning("Chưa có mặt hàng", "Yêu cầu nhập hàng phải có ít nhất một mặt hàng.");
            return;
        }

        try {
            OrderRequestCreationDTO dto = new OrderRequestCreationDTO(
                    null,
                    dpDesiredDeliveryDate.getValue().toString(),
                    buildItemDTOs()
            );

            String requestBody = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                OrderRequestDetailDTO createdRequest =
                        objectMapper.readValue(response.body(), OrderRequestDetailDTO.class);

                showInfo(
                        "Tạo yêu cầu nhập hàng thành công",
                        "Mã yêu cầu: " + createdRequest.getRequestCode()
                                + "\nTrạng thái: " + createdRequest.getStatus()
                                + "\nSố mặt hàng: " + createdRequest.getItems().size()
                );

                clearForm();
                loadRequests();
            } else {
                showError("Tạo yêu cầu nhập hàng thất bại", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API tạo yêu cầu nhập hàng: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API tạo yêu cầu nhập hàng: " + e.getMessage());
        }
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

    private void loadRequests() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
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

    private List<OrderRequestCreationDTO.ItemDTO> buildItemDTOs() {
        return requestItems.stream()
                .map(row -> new OrderRequestCreationDTO.ItemDTO(
                        row.getMerchandiseCode(),
                        row.getQuantity()
                ))
                .toList();
    }

    private void clearForm() {
        dpDesiredDeliveryDate.setValue(null);
        cboMerchandise.getSelectionModel().clearSelection();
        spnQuantity.getValueFactory().setValue(1);
        requestItems.clear();
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
