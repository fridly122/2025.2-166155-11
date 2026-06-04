package itss.group11.controller.uc3;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;

import itss.group11.entity.uc1.MerchandiseOptionDTO;
import itss.group11.entity.uc3.InventoryRowDTO;
import itss.group11.entity.uc3.InventoryUpdateDTO;
import itss.group11.entity.uc3.SiteOptionDTO;
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
import javafx.scene.layout.Region;
import javafx.util.StringConverter;

public class SiteInventoryManageController {

    @FXML
    private ComboBox<SiteOptionDTO> cboSite;

    @FXML
    private ComboBox<MerchandiseOptionDTO> cboMerchandise;

    @FXML
    private Spinner<Integer> spnInStockQuantity;

    @FXML
    private Button btnUpdateInventory;

    @FXML
    private TableView<InventoryRow> inventoryTable;

    @FXML
    private TableColumn<InventoryRow, Long> colId;

    @FXML
    private TableColumn<InventoryRow, String> colSiteCode;

    @FXML
    private TableColumn<InventoryRow, String> colSiteName;

    @FXML
    private TableColumn<InventoryRow, String> colMerchandiseCode;

    @FXML
    private TableColumn<InventoryRow, String> colMerchandiseName;

    @FXML
    private TableColumn<InventoryRow, Integer> colQuantity;

    @FXML
    private TableColumn<InventoryRow, String> colUnit;

    @FXML
    private TableColumn<InventoryRow, String> colStockStatus;

    @FXML
    private Label lblSelectedInventory;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL = ApiConfig.baseUrl("/api/v1/warehouse");

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupInventoryTable();

        inventoryTable.setPlaceholder(new Label("Chưa có dữ liệu tồn kho."));
        lblSelectedInventory.setText("Chưa chọn dòng tồn kho.");

        inventoryTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, selected) -> fillFormFromSelectedRow(selected));

        loadOptions();
        loadInventoryRows();
    }

    @FXML
    private void handleRefresh() {
        loadOptions();
        loadInventoryRows();
        clearForm();
    }

    @FXML
    private void handleUpdateInventory() {
        SiteOptionDTO selectedSite = cboSite.getSelectionModel().getSelectedItem();
        MerchandiseOptionDTO selectedMerchandise = cboMerchandise.getSelectionModel().getSelectedItem();
        Integer quantity = spnInStockQuantity.getValue();

        if (selectedSite == null) {
            showWarning("Chưa chọn site", "Vui lòng chọn site cần cập nhật tồn kho.");
            return;
        }

        if (selectedMerchandise == null) {
            showWarning("Chưa chọn mặt hàng", "Vui lòng chọn mặt hàng cần cập nhật tồn kho.");
            return;
        }

        if (quantity == null || quantity < 0) {
            showWarning("Số lượng không hợp lệ", "Số lượng tồn kho phải lớn hơn hoặc bằng 0.");
            return;
        }

        try {
            InventoryUpdateDTO dto = new InventoryUpdateDTO(
                    selectedSite.getSiteCode(),
                    selectedMerchandise.getCode(),
                    quantity
            );

            String requestBody = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/inventory"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                InventoryRowDTO result =
                        objectMapper.readValue(response.body(), InventoryRowDTO.class);

                showInfo(
                        "Cập nhật tồn kho thành công",
                        result.getSiteCode() + " - " + result.getMerchandiseCode()
                                + "\nSố lượng tồn: " + result.getInStockQuantity()
                                + "\nTrạng thái: " + result.getStockStatus()
                );

                loadInventoryRows();
                selectUpdatedRow(result);
            } else {
                showError("Cập nhật tồn kho thất bại", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API cập nhật tồn kho: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API cập nhật tồn kho: " + e.getMessage());
        }
    }

    private void setupComboBoxes() {
        cboSite.setConverter(new StringConverter<>() {
            @Override
            public String toString(SiteOptionDTO site) {
                if (site == null) {
                    return "";
                }

                return site.getSiteCode() + " - " + site.getSiteName();
            }

            @Override
            public SiteOptionDTO fromString(String value) {
                return null;
            }
        });

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

    private void setupInventoryTable() {
        colId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colSiteCode.setCellValueFactory(data -> data.getValue().siteCodeProperty());
        colSiteName.setCellValueFactory(data -> data.getValue().siteNameProperty());
        colMerchandiseCode.setCellValueFactory(data -> data.getValue().merchandiseCodeProperty());
        colMerchandiseName.setCellValueFactory(data -> data.getValue().merchandiseNameProperty());
        colQuantity.setCellValueFactory(data -> data.getValue().quantityProperty().asObject());
        colUnit.setCellValueFactory(data -> data.getValue().unitProperty());
        colStockStatus.setCellValueFactory(data -> data.getValue().stockStatusProperty());
    }

    private void loadOptions() {
        loadSiteOptions();
        loadMerchandiseOptions();
    }

    private void loadSiteOptions() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/sites"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                SiteOptionDTO[] sites =
                        objectMapper.readValue(response.body(), SiteOptionDTO[].class);

                cboSite.setItems(FXCollections.observableArrayList(sites));
            } else {
                showError("Không thể tải danh sách site", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API danh sách site: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API danh sách site: " + e.getMessage());
        }
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

    private void loadInventoryRows() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/inventory"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                InventoryRowDTO[] rows =
                        objectMapper.readValue(response.body(), InventoryRowDTO[].class);

                inventoryTable.setItems(FXCollections.observableArrayList(
                        Arrays.stream(rows)
                                .map(this::toInventoryRow)
                                .toList()
                ));
            } else {
                showError("Không thể tải danh sách tồn kho", response.body());
            }

        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API danh sách tồn kho: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API danh sách tồn kho: " + e.getMessage());
        }
    }

    private InventoryRow toInventoryRow(InventoryRowDTO dto) {
        return new InventoryRow(
                dto.getId(),
                dto.getSiteCode(),
                dto.getSiteName(),
                dto.getMerchandiseCode(),
                dto.getMerchandiseName(),
                dto.getInStockQuantity(),
                dto.getUnit(),
                dto.getStockStatus()
        );
    }

    private void fillFormFromSelectedRow(InventoryRow selected) {
        if (selected == null) {
            lblSelectedInventory.setText("Chưa chọn dòng tồn kho.");
            return;
        }

        lblSelectedInventory.setText(
                "Đang chọn: " + selected.getSiteCode()
                        + " | " + selected.getMerchandiseCode()
                        + " | " + selected.getStockStatus()
        );

        selectComboSite(selected.getSiteCode());
        selectComboMerchandise(selected.getMerchandiseCode());
        spnInStockQuantity.getValueFactory().setValue(selected.getQuantity());
    }

    private void selectComboSite(String siteCode) {
        if (cboSite.getItems() == null) {
            return;
        }

        cboSite.getItems()
                .stream()
                .filter(site -> site.getSiteCode().equals(siteCode))
                .findFirst()
                .ifPresent(site -> cboSite.getSelectionModel().select(site));
    }

    private void selectComboMerchandise(String merchandiseCode) {
        if (cboMerchandise.getItems() == null) {
            return;
        }

        cboMerchandise.getItems()
                .stream()
                .filter(merchandise -> merchandise.getCode().equals(merchandiseCode))
                .findFirst()
                .ifPresent(merchandise -> cboMerchandise.getSelectionModel().select(merchandise));
    }

    private void selectUpdatedRow(InventoryRowDTO result) {
        for (InventoryRow row : inventoryTable.getItems()) {
            if (row.getSiteCode().equals(result.getSiteCode())
                    && row.getMerchandiseCode().equals(result.getMerchandiseCode())) {
                inventoryTable.getSelectionModel().select(row);
                inventoryTable.scrollTo(row);
                return;
            }
        }
    }

    private void clearForm() {
        cboSite.getSelectionModel().clearSelection();
        cboMerchandise.getSelectionModel().clearSelection();
        spnInStockQuantity.getValueFactory().setValue(0);
        inventoryTable.getSelectionModel().clearSelection();
        lblSelectedInventory.setText("Chưa chọn dòng tồn kho.");
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

    public static class InventoryRow {
        private final SimpleLongProperty id;
        private final SimpleStringProperty siteCode;
        private final SimpleStringProperty siteName;
        private final SimpleStringProperty merchandiseCode;
        private final SimpleStringProperty merchandiseName;
        private final SimpleIntegerProperty quantity;
        private final SimpleStringProperty unit;
        private final SimpleStringProperty stockStatus;

        public InventoryRow(
                Long id,
                String siteCode,
                String siteName,
                String merchandiseCode,
                String merchandiseName,
                Integer quantity,
                String unit,
                String stockStatus
        ) {
            this.id = new SimpleLongProperty(id == null ? 0L : id);
            this.siteCode = new SimpleStringProperty(siteCode == null ? "" : siteCode);
            this.siteName = new SimpleStringProperty(siteName == null ? "" : siteName);
            this.merchandiseCode = new SimpleStringProperty(merchandiseCode == null ? "" : merchandiseCode);
            this.merchandiseName = new SimpleStringProperty(merchandiseName == null ? "" : merchandiseName);
            this.quantity = new SimpleIntegerProperty(quantity == null ? 0 : quantity);
            this.unit = new SimpleStringProperty(unit == null ? "" : unit);
            this.stockStatus = new SimpleStringProperty(stockStatus == null ? "" : stockStatus);
        }

        public String getSiteCode() {
            return siteCode.get();
        }

        public String getMerchandiseCode() {
            return merchandiseCode.get();
        }

        public Integer getQuantity() {
            return quantity.get();
        }

        public String getStockStatus() {
            return stockStatus.get();
        }

        public SimpleLongProperty idProperty() {
            return id;
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

        public SimpleIntegerProperty quantityProperty() {
            return quantity;
        }

        public SimpleStringProperty unitProperty() {
            return unit;
        }

        public SimpleStringProperty stockStatusProperty() {
            return stockStatus;
        }
    }
}

