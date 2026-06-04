package itss.group11.controller.uc4;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;

import itss.group11.entity.uc4.PendingPurchaseOrderDTO;
import itss.group11.entity.uc4.TransportCreateDTO;
import itss.group11.entity.uc4.TransportDetailDTO;
import itss.group11.entity.uc4.TransportUpdateDTO;
import itss.group11.controller.chung.ApiConfig;
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

        pendingOrderTable.setPlaceholder(new Label("KhÃ´ng cÃ³ Ä‘Æ¡n hÃ ng CREATED chá» váº­n chuyá»ƒn."));
        transportTable.setPlaceholder(new Label("ChÆ°a cÃ³ thÃ´ng tin váº­n chuyá»ƒn."));

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
            showWarning("ChÆ°a chá»n Ä‘Æ¡n hÃ ng", "Vui lÃ²ng chá»n má»™t Ä‘Æ¡n hÃ ng trÆ°á»›c khi táº¡o váº­n chuyá»ƒn.");
            return;
        }

        String destinationSite = txtDestinationSite.getText();
        String vehicle = cboVehicle.getSelectionModel().getSelectedItem();
        Integer deliveryDays = spnDeliveryDays.getValue();

        if (destinationSite == null || destinationSite.isBlank()) {
            showWarning("Thiáº¿u thÃ´ng tin", "Site Ä‘Ã­ch khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
            return;
        }

        if (vehicle == null || vehicle.isBlank()) {
            showWarning("Thiáº¿u thÃ´ng tin", "PhÆ°Æ¡ng tiá»‡n váº­n chuyá»ƒn khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
            return;
        }

        if (deliveryDays == null || deliveryDays <= 0 || deliveryDays > 365) {
            showWarning("Dá»¯ liá»‡u khÃ´ng há»£p lá»‡", "Sá»‘ ngÃ y váº­n chuyá»ƒn pháº£i lá»›n hÆ¡n 0 vÃ  khÃ´ng quÃ¡ 365.");
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
                        "Táº¡o váº­n chuyá»ƒn thÃ nh cÃ´ng",
                        "ÄÃ£ táº¡o váº­n chuyá»ƒn cho Ä‘Æ¡n hÃ ng " + result.getOrderId()
                                + "\nTráº¡ng thÃ¡i váº­n chuyá»ƒn: " + result.getTransportStatus()
                                + "\nTráº¡ng thÃ¡i PO: " + result.getPurchaseOrderStatus()
                );

                clearCreateForm();
                loadPendingOrders();
                loadTransports();
            } else {
                showError("Táº¡o váº­n chuyá»ƒn tháº¥t báº¡i", response.body());
            }

        } catch (IOException e) {
            showError("Lá»—i káº¿t ná»‘i", "KhÃ´ng gá»i Ä‘Æ°á»£c API táº¡o váº­n chuyá»ƒn: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lá»—i káº¿t ná»‘i", "KhÃ´ng gá»i Ä‘Æ°á»£c API táº¡o váº­n chuyá»ƒn: " + e.getMessage());
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
            showWarning("ChÆ°a chá»n váº­n chuyá»ƒn", "Vui lÃ²ng chá»n má»™t dÃ²ng váº­n chuyá»ƒn trÆ°á»›c khi cáº­p nháº­t.");
            return;
        }

        if (destinationSite == null || destinationSite.isBlank()) {
            showWarning("Thiáº¿u thÃ´ng tin", "Site Ä‘Ã­ch khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
            return;
        }

        if (vehicle == null || vehicle.isBlank()) {
            showWarning("Thiáº¿u thÃ´ng tin", "PhÆ°Æ¡ng tiá»‡n váº­n chuyá»ƒn khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
            return;
        }

        if (deliveryDays == null || deliveryDays <= 0 || deliveryDays > 365) {
            showWarning("Dá»¯ liá»‡u khÃ´ng há»£p lá»‡", "Sá»‘ ngÃ y váº­n chuyá»ƒn pháº£i lá»›n hÆ¡n 0 vÃ  khÃ´ng quÃ¡ 365.");
            return;
        }

        if (newStatus == null || newStatus.isBlank()) {
            showWarning("ChÆ°a chá»n tráº¡ng thÃ¡i", "Vui lÃ²ng chá»n tráº¡ng thÃ¡i váº­n chuyá»ƒn má»›i.");
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
                        "Cáº­p nháº­t váº­n chuyá»ƒn thÃ nh cÃ´ng",
                        "MÃ£ PO: " + result.getOrderId()
                                + "\nTráº¡ng thÃ¡i váº­n chuyá»ƒn: " + result.getTransportStatus()
                                + "\nTráº¡ng thÃ¡i PO: " + result.getPurchaseOrderStatus()
                );

                loadTransports();
                loadPendingOrders();
            } else {
                showError("Cáº­p nháº­t váº­n chuyá»ƒn tháº¥t báº¡i", response.body());
            }

        } catch (IOException e) {
            showError("Lá»—i káº¿t ná»‘i", "KhÃ´ng gá»i Ä‘Æ°á»£c API cáº­p nháº­t váº­n chuyá»ƒn: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lá»—i káº¿t ná»‘i", "KhÃ´ng gá»i Ä‘Æ°á»£c API cáº­p nháº­t váº­n chuyá»ƒn: " + e.getMessage());
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
                showError("KhÃ´ng thá»ƒ táº£i danh sÃ¡ch Ä‘Æ¡n hÃ ng", response.body());
            }

        } catch (IOException e) {
            showError("Lá»—i káº¿t ná»‘i", "KhÃ´ng gá»i Ä‘Æ°á»£c API danh sÃ¡ch Ä‘Æ¡n hÃ ng: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lá»—i káº¿t ná»‘i", "KhÃ´ng gá»i Ä‘Æ°á»£c API danh sÃ¡ch Ä‘Æ¡n hÃ ng: " + e.getMessage());
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
                showError("KhÃ´ng thá»ƒ táº£i danh sÃ¡ch váº­n chuyá»ƒn", response.body());
            }

        } catch (IOException e) {
            showError("Lá»—i káº¿t ná»‘i", "KhÃ´ng gá»i Ä‘Æ°á»£c API danh sÃ¡ch váº­n chuyá»ƒn: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lá»—i káº¿t ná»‘i", "KhÃ´ng gá»i Ä‘Æ°á»£c API danh sÃ¡ch váº­n chuyá»ƒn: " + e.getMessage());
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
            lblSelectedOrder.setText("ChÆ°a chá»n Ä‘Æ¡n hÃ ng");
        } else {
            lblSelectedOrder.setText("Äang chá»n: " + selected.getOrderId() + " | " + selected.getSourceSite());
        }
    }

    private void updateSelectedTransportLabel(TransportRow selected) {
        if (selected == null) {
            lblSelectedTransport.setText("ChÆ°a chá»n thÃ´ng tin váº­n chuyá»ƒn");
            txtEditDestinationSite.clear();
            cboEditVehicle.getSelectionModel().clearSelection();
            spnEditDeliveryDays.getValueFactory().setValue(1);
            cboTransportStatus.getSelectionModel().clearSelection();
            return;
        }

        lblSelectedTransport.setText(
                "Äang chá»n: " + selected.getOrderId()
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
        lblSelectedOrder.setText("ChÆ°a chá»n Ä‘Æ¡n hÃ ng");
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

