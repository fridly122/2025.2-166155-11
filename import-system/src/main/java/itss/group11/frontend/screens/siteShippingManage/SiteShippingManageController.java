package itss.group11.frontend.screens.siteShippingManage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;

import itss.group11.dto.transport.PendingPurchaseOrderDTO;
import itss.group11.dto.transport.TransportCreateDTO;
import itss.group11.dto.transport.TransportDetailDTO;
import itss.group11.dto.transport.TransportUpdateDTO;
import itss.group11.frontend.ApiConfig;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;

public class SiteShippingManageController {

    @FXML
    private TableView<PendingOrderRow> pendingOrderTable;

    @FXML
    private TableColumn<PendingOrderRow, String> colOrderId;

    @FXML
    private TableColumn<PendingOrderRow, String> colRequestCode;

    @FXML
    private TableColumn<PendingOrderRow, String> colSourceSite;

    @FXML
    private TableColumn<PendingOrderRow, String> colStatus;

    @FXML
    private TableColumn<PendingOrderRow, String> colCreatedDate;

    @FXML
    private TextField txtDestinationSite;

    @FXML
    private ComboBox<String> cboVehicle;

    @FXML
    private Spinner<Integer> spnDeliveryDays;

    @FXML
    private Button btnCreateTransport;

    @FXML
    private Label lblSelectedOrder;

    @FXML
    private TableView<TransportRow> transportTable;

    @FXML
    private TableColumn<TransportRow, Long> colTransportId;

    @FXML
    private TableColumn<TransportRow, String> colTransportOrderId;

    @FXML
    private TableColumn<TransportRow, String> colTransportSourceSite;

    @FXML
    private TableColumn<TransportRow, String> colDestinationSite;

    @FXML
    private TableColumn<TransportRow, String> colVehicle;

    @FXML
    private TableColumn<TransportRow, Integer> colDeliveryDays;

    @FXML
    private TableColumn<TransportRow, String> colTransportStatus;

    @FXML
    private TableColumn<TransportRow, String> colPurchaseOrderStatus;

    @FXML
    private TextField txtEditDestinationSite;

    @FXML
    private ComboBox<String> cboEditVehicle;

    @FXML
    private Spinner<Integer> spnEditDeliveryDays;

    @FXML
    private ComboBox<String> cboTransportStatus;

    @FXML
    private Button btnUpdateTransportStatus;

    @FXML
    private Label lblSelectedTransport;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL = ApiConfig.baseUrl("/api/v1/transports");
    private static final String VEHICLE_AIR = "AIR";
    private static final String VEHICLE_SHIP = "SHIP";
    private static final String VEHICLE_TRUCK = "TRUCK";

    @FXML
    public void initialize() {
        setupPendingOrderTable();
        setupTransportTable();

        pendingOrderTable.setPlaceholder(new Label("Không có đơn hàng CREATED chờ vận chuyển."));
        transportTable.setPlaceholder(new Label("Chưa có thông tin vận chuyển."));

        cboVehicle.setItems(FXCollections.observableArrayList(VEHICLE_AIR, VEHICLE_SHIP, VEHICLE_TRUCK));
        cboEditVehicle.setItems(FXCollections.observableArrayList(VEHICLE_AIR, VEHICLE_SHIP, VEHICLE_TRUCK));
        cboVehicle.getSelectionModel().select(VEHICLE_AIR);
        cboTransportStatus.setItems(FXCollections.observableArrayList("IN_TRANSIT", "COMPLETED", "CANCELLED"));

        btnCreateTransport.disableProperty().bind(
                pendingOrderTable.getSelectionModel().selectedItemProperty().isNull()
        );
        btnUpdateTransportStatus.disableProperty().bind(
                transportTable.getSelectionModel().selectedItemProperty().isNull()
        );

        pendingOrderTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, selected) -> updateSelectedOrderLabel(selected));

        transportTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, selected) -> updateSelectedTransportLabel(selected));

        loadPendingOrders();
        loadTransports();
    }

    @FXML
    private void handleCreateTransport() {
        PendingOrderRow selected = pendingOrderTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Chưa chọn đơn hàng", "Vui lòng chọn một đơn hàng trước khi tạo vận chuyển.");
            return;
        }

        String destinationSite = txtDestinationSite.getText();
        String vehicle = cboVehicle.getSelectionModel().getSelectedItem();
        Integer deliveryDays = spnDeliveryDays.getValue();

        if (destinationSite == null || destinationSite.isBlank()) {
            showWarning("Thiếu thông tin", "Site đích không được để trống.");
            return;
        }

        if (vehicle == null || vehicle.isBlank()) {
            showWarning("Thiếu thông tin", "Phương tiện vận chuyển không được để trống.");
            return;
        }

        if (deliveryDays == null || deliveryDays <= 0 || deliveryDays > 365) {
            showWarning("Dữ liệu không hợp lệ", "Số ngày vận chuyển phải lớn hơn 0 và không quá 365.");
            return;
        }

        try {
            TransportCreateDTO dto = new TransportCreateDTO(
                    selected.getOrderId(),
                    destinationSite.trim(),
                    deliveryDays,
                    vehicle.trim()
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
                TransportDetailDTO result =
                        objectMapper.readValue(response.body(), TransportDetailDTO.class);

                showInfo(
                        "Tạo vận chuyển thành công",
                        "Đã tạo vận chuyển cho đơn hàng " + result.getOrderId()
                                + "\nTrạng thái vận chuyển: " + result.getTransportStatus()
                                + "\nTrạng thái PO: " + result.getPurchaseOrderStatus()
                );

                clearCreateForm();
                loadPendingOrders();
                loadTransports();
            } else {
                showError("Tạo vận chuyển thất bại", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API tạo vận chuyển: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API tạo vận chuyển: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateTransport() {
        TransportRow selected = transportTable.getSelectionModel().getSelectedItem();
        String newStatus = cboTransportStatus.getSelectionModel().getSelectedItem();
        String destinationSite = txtEditDestinationSite.getText();
        String vehicle = cboEditVehicle.getSelectionModel().getSelectedItem();
        Integer deliveryDays = spnEditDeliveryDays.getValue();

        if (selected == null) {
            showWarning("Chưa chọn vận chuyển", "Vui lòng chọn một dòng vận chuyển trước khi cập nhật.");
            return;
        }

        if (destinationSite == null || destinationSite.isBlank()) {
            showWarning("Thiếu thông tin", "Site đích không được để trống.");
            return;
        }

        if (vehicle == null || vehicle.isBlank()) {
            showWarning("Thiếu thông tin", "Phương tiện vận chuyển không được để trống.");
            return;
        }

        if (deliveryDays == null || deliveryDays <= 0 || deliveryDays > 365) {
            showWarning("Dữ liệu không hợp lệ", "Số ngày vận chuyển phải lớn hơn 0 và không quá 365.");
            return;
        }

        if (newStatus == null || newStatus.isBlank()) {
            showWarning("Chưa chọn trạng thái", "Vui lòng chọn trạng thái vận chuyển mới.");
            return;
        }

        try {
            TransportUpdateDTO dto = new TransportUpdateDTO(
                    destinationSite.trim(),
                    deliveryDays,
                    vehicle.trim(),
                    newStatus
            );
            String requestBody = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + selected.getId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                TransportDetailDTO result =
                        objectMapper.readValue(response.body(), TransportDetailDTO.class);

                showInfo(
                        "Cập nhật vận chuyển thành công",
                        "Mã PO: " + result.getOrderId()
                                + "\nTrạng thái vận chuyển: " + result.getTransportStatus()
                                + "\nTrạng thái PO: " + result.getPurchaseOrderStatus()
                );

                loadTransports();
                loadPendingOrders();
            } else {
                showError("Cập nhật vận chuyển thất bại", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API cập nhật vận chuyển: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API cập nhật vận chuyển: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateTransportStatus() {
        handleUpdateTransport();
    }

    @FXML
    private void handleRefresh() {
        loadPendingOrders();
        loadTransports();
    }

    private void setupPendingOrderTable() {
        colOrderId.setCellValueFactory(data -> data.getValue().orderIdProperty());
        colRequestCode.setCellValueFactory(data -> data.getValue().requestCodeProperty());
        colSourceSite.setCellValueFactory(data -> data.getValue().sourceSiteProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colCreatedDate.setCellValueFactory(data -> data.getValue().createdDateProperty());
    }

    private void setupTransportTable() {
        colTransportId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colTransportOrderId.setCellValueFactory(data -> data.getValue().orderIdProperty());
        colTransportSourceSite.setCellValueFactory(data -> data.getValue().sourceSiteProperty());
        colDestinationSite.setCellValueFactory(data -> data.getValue().destinationSiteProperty());
        colVehicle.setCellValueFactory(data -> data.getValue().vehicleProperty());
        colDeliveryDays.setCellValueFactory(data -> data.getValue().deliveryDaysProperty().asObject());
        colTransportStatus.setCellValueFactory(data -> data.getValue().transportStatusProperty());
        colPurchaseOrderStatus.setCellValueFactory(data -> data.getValue().purchaseOrderStatusProperty());
    }

    private void loadPendingOrders() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/pending-orders"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                PendingPurchaseOrderDTO[] rows =
                        objectMapper.readValue(response.body(), PendingPurchaseOrderDTO[].class);

                pendingOrderTable.setItems(FXCollections.observableArrayList(
                        Arrays.stream(rows)
                                .map(row -> new PendingOrderRow(
                                        row.getOrderId(),
                                        row.getRequestCode(),
                                        row.getSiteCode() + " - " + row.getSiteName(),
                                        row.getStatus(),
                                        row.getCreatedDate()
                                ))
                                .toList()
                ));
            } else {
                showError("Không thể tải danh sách đơn hàng", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API danh sách đơn hàng: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API danh sách đơn hàng: " + e.getMessage());
        }
    }

    private void loadTransports() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                TransportDetailDTO[] rows =
                        objectMapper.readValue(response.body(), TransportDetailDTO[].class);

                transportTable.setItems(FXCollections.observableArrayList(
                        Arrays.stream(rows)
                                .map(this::toTransportRow)
                                .toList()
                ));
            } else {
                showError("Không thể tải danh sách vận chuyển", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API danh sách vận chuyển: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API danh sách vận chuyển: " + e.getMessage());
        }
    }

    private TransportRow toTransportRow(TransportDetailDTO dto) {
        return new TransportRow(
                dto.getId(),
                dto.getOrderId(),
                dto.getSourceSiteCode() + " - " + dto.getSourceSiteName(),
                dto.getDestinationSiteName(),
                dto.getVehicle(),
                dto.getDeliveryDays(),
                dto.getTransportStatus(),
                dto.getPurchaseOrderStatus()
        );
    }

    private void updateSelectedOrderLabel(PendingOrderRow selected) {
        if (selected == null) {
            lblSelectedOrder.setText("Chưa chọn đơn hàng");
        } else {
            lblSelectedOrder.setText("Đang chọn: " + selected.getOrderId() + " | " + selected.getSourceSite());
        }
    }

    private void updateSelectedTransportLabel(TransportRow selected) {
        if (selected == null) {
            lblSelectedTransport.setText("Chưa chọn thông tin vận chuyển");
            txtEditDestinationSite.clear();
            cboEditVehicle.getSelectionModel().clearSelection();
            spnEditDeliveryDays.getValueFactory().setValue(1);
            cboTransportStatus.getSelectionModel().clearSelection();
            return;
        }

        lblSelectedTransport.setText(
                "Đang chọn: " + selected.getOrderId()
                + " | " + selected.getSourceSite()
                        + " -> " + selected.getDestinationSite()
        );
        txtEditDestinationSite.setText(selected.getDestinationSite());
        selectVehicle(cboEditVehicle, selected.getVehicle());
        spnEditDeliveryDays.getValueFactory().setValue(Math.max(1, selected.getDeliveryDays()));
        cboTransportStatus.getSelectionModel().select(selected.getTransportStatus());
    }

    private void clearCreateForm() {
        txtDestinationSite.clear();
        cboVehicle.getSelectionModel().select(VEHICLE_AIR);
        spnDeliveryDays.getValueFactory().setValue(1);
        pendingOrderTable.getSelectionModel().clearSelection();
        lblSelectedOrder.setText("Chưa chọn đơn hàng");
    }

    private void selectVehicle(ComboBox<String> comboBox, String vehicle) {
        if (vehicle == null || vehicle.isBlank()) {
            comboBox.getSelectionModel().clearSelection();
            return;
        }

        String normalizedVehicle = vehicle.trim().toUpperCase();
        if (comboBox.getItems().contains(normalizedVehicle)) {
            comboBox.getSelectionModel().select(normalizedVehicle);
        } else {
            comboBox.getSelectionModel().clearSelection();
        }
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

    public static class PendingOrderRow {
        private final SimpleStringProperty orderId;
        private final SimpleStringProperty requestCode;
        private final SimpleStringProperty sourceSite;
        private final SimpleStringProperty status;
        private final SimpleStringProperty createdDate;

        public PendingOrderRow(
                String orderId,
                String requestCode,
                String sourceSite,
                String status,
                String createdDate
        ) {
            this.orderId = new SimpleStringProperty(orderId == null ? "" : orderId);
            this.requestCode = new SimpleStringProperty(requestCode == null ? "" : requestCode);
            this.sourceSite = new SimpleStringProperty(sourceSite == null ? "" : sourceSite);
            this.status = new SimpleStringProperty(status == null ? "" : status);
            this.createdDate = new SimpleStringProperty(createdDate == null ? "" : createdDate);
        }

        public String getOrderId() {
            return orderId.get();
        }

        public String getSourceSite() {
            return sourceSite.get();
        }

        public SimpleStringProperty orderIdProperty() {
            return orderId;
        }

        public SimpleStringProperty requestCodeProperty() {
            return requestCode;
        }

        public SimpleStringProperty sourceSiteProperty() {
            return sourceSite;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        public SimpleStringProperty createdDateProperty() {
            return createdDate;
        }
    }

    public static class TransportRow {
        private final SimpleLongProperty id;
        private final SimpleStringProperty orderId;
        private final SimpleStringProperty sourceSite;
        private final SimpleStringProperty destinationSite;
        private final SimpleStringProperty vehicle;
        private final SimpleIntegerProperty deliveryDays;
        private final SimpleStringProperty transportStatus;
        private final SimpleStringProperty purchaseOrderStatus;

        public TransportRow(
                Long id,
                String orderId,
                String sourceSite,
                String destinationSite,
                String vehicle,
                Integer deliveryDays,
                String transportStatus,
                String purchaseOrderStatus
        ) {
            this.id = new SimpleLongProperty(id == null ? 0L : id);
            this.orderId = new SimpleStringProperty(orderId == null ? "" : orderId);
            this.sourceSite = new SimpleStringProperty(sourceSite == null ? "" : sourceSite);
            this.destinationSite = new SimpleStringProperty(destinationSite == null ? "" : destinationSite);
            this.vehicle = new SimpleStringProperty(vehicle == null ? "" : vehicle);
            this.deliveryDays = new SimpleIntegerProperty(deliveryDays == null ? 0 : deliveryDays);
            this.transportStatus = new SimpleStringProperty(transportStatus == null ? "" : transportStatus);
            this.purchaseOrderStatus = new SimpleStringProperty(purchaseOrderStatus == null ? "" : purchaseOrderStatus);
        }

        public Long getId() {
            return id.get();
        }

        public String getOrderId() {
            return orderId.get();
        }

        public String getSourceSite() {
            return sourceSite.get();
        }

        public String getDestinationSite() {
            return destinationSite.get();
        }

        public String getVehicle() {
            return vehicle.get();
        }

        public int getDeliveryDays() {
            return deliveryDays.get();
        }

        public String getTransportStatus() {
            return transportStatus.get();
        }

        public SimpleLongProperty idProperty() {
            return id;
        }

        public SimpleStringProperty orderIdProperty() {
            return orderId;
        }

        public SimpleStringProperty sourceSiteProperty() {
            return sourceSite;
        }

        public SimpleStringProperty destinationSiteProperty() {
            return destinationSite;
        }

        public SimpleStringProperty vehicleProperty() {
            return vehicle;
        }

        public SimpleIntegerProperty deliveryDaysProperty() {
            return deliveryDays;
        }

        public SimpleStringProperty transportStatusProperty() {
            return transportStatus;
        }

        public SimpleStringProperty purchaseOrderStatusProperty() {
            return purchaseOrderStatus;
        }
    }
}
