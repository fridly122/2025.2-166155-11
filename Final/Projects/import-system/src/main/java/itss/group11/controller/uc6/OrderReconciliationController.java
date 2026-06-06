package itss.group11.controller.uc6;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import itss.group11.entity.uc6.PartialOrderSelectionDTO;
import itss.group11.entity.uc6.ReceivedLineDTO;
import itss.group11.entity.uc6.ReconciliationDetailDTO;
import itss.group11.entity.uc6.ReconciliationResultDTO;
import itss.group11.entity.uc6.ReconciliationSubmitDTO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.util.converter.IntegerStringConverter;

public class OrderReconciliationController {

    @FXML
    private TextField txtOrderFilter;

    @FXML
    private DatePicker dpCreatedDateFilter;

    @FXML
    private ComboBox<String> cbSiteFilter;

    @FXML
    private ListView<PurchaseOrderRow> orderList;

    @FXML
    private Label lblOrderCount;

    @FXML
    private Label lblSelectedPo;

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
    private TableColumn<LineRow, String> colLineStatus;

    @FXML
    private Button btnRecalculate;

    @FXML
    private Button btnCreateReport;

    @FXML
    private Button btnConfirm;

    @FXML
    private Label lblDetailOrderId;

    @FXML
    private Label lblDetailRequestCode;

    @FXML
    private Label lblDetailSupplier;

    @FXML
    private Label lblDetailSite;

    @FXML
    private Label lblDetailStatus;

    @FXML
    private Label lblDetailItemCount;

    @FXML
    private Label lblDetailDiscrepancyCount;

    @FXML
    private TextArea txtReportPreview;

    @FXML
    private Button btnEditReport;

    @FXML
    private TextArea txtReceivingNotePreview;

    @FXML
    private Button btnEditReceivingNote;

    @FXML
    private Label lblReceivingStatus;

    private ReconciliationApiClient apiClient = new HttpReconciliationApiClient();
    private final ObservableList<PurchaseOrderRow> filteredOrders = FXCollections.observableArrayList();
    private final List<PurchaseOrderRow> allOrders = new ArrayList<>();

    private PurchaseOrderRow selectedOrder;
    private DiscrepancyInput discrepancyInput;
    private ReceivingNoteInput receivingNoteInput;
    private boolean confirmed;

    void setApiClient(ReconciliationApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @FXML
    public void initialize() {
        setupOrderList();
        setupLineTable();
        setupFilters();
        setupReadonlyPanels();
        resetRightPanel();

        updateReportButtonVisibility();

        orderList.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, selected) -> handleOrderSelected(selected));

        loadInTransitOrders();
    }

    @FXML
    private void handleRefresh() {
        loadInTransitOrders();
    }

    @FXML
    private void handleClearFilters() {
        txtOrderFilter.clear();
        dpCreatedDateFilter.setValue(null);
        cbSiteFilter.setValue(null);
        applyFilters();
    }

    @FXML
    private void handleRecalculateDiscrepancy() {
        recalculateDiscrepancy();
        if (hasDiscrepancy()) {
            showInfo("Đã tính lại sai lệch", "Bảng đối soát đã cập nhật chênh lệch số lượng.");
        } else {
            showInfo("Đã tính lại sai lệch", "Không có dòng hàng sai lệch.");
        }
    }

    @FXML
    private void handleCreateReport() {
        if (selectedOrder == null) {
            showWarning("Chưa chọn đơn", "Vui lòng chọn một đơn đang vận chuyển.");
            return;
        }
        recalculateDiscrepancy();
        if (!hasDiscrepancy()) {
            showWarning("Không có sai lệch", "Chỉ lập biên bản khi có dòng hàng sai lệch.");
            return;
        }

        Optional<DiscrepancyInput> input = showDiscrepancyDialog(discrepancyInput);
        input.ifPresent(value -> {
            discrepancyInput = value;
            renderReportPanel();
        });
    }

    @FXML
    private void handleEditReport() {
        handleCreateReport();
    }

    @FXML
    private void handleEditReceivingNote() {
        Optional<ReceivingNoteInput> input = showReceivingNoteDialog(receivingNoteInput);
        input.ifPresent(value -> {
            receivingNoteInput = value;
            renderReceivingNotePanel();
        });
    }

    @FXML
    private void handleConfirmReconciliation() {
        if (selectedOrder == null) {
            showWarning("Chưa chọn đơn", "Vui lòng chọn một đơn đang vận chuyển trước khi xác nhận nhập kho.");
            return;
        }
        if (lineTable.getItems() == null || lineTable.getItems().isEmpty()) {
            showWarning("Chưa có dòng hàng", "Đơn hàng chưa có dòng hàng để đối soát.");
            return;
        }

        recalculateDiscrepancy();
        if (hasDiscrepancy() && discrepancyInput == null) {
            showWarning("Chưa lập biên bản sai lệch", "Đơn đang có sai lệch. Vui lòng lập biên bản sai lệch trước khi xác nhận nhập kho.");
            return;
        }

        Optional<ReceivingNoteInput> noteInput = showReceivingNoteDialog(receivingNoteInput);
        if (noteInput.isEmpty()) {
            return;
        }
        receivingNoteInput = noteInput.get();
        renderReceivingNotePanel();

        try {
            ReconciliationSubmitDTO dto = buildSubmitDTO();
            ReconciliationResultDTO result = apiClient.reconcile(selectedOrder.getOrderId(), dto);

            confirmed = true;
            lblDetailStatus.setText(result.getStatus());
            lblReceivingStatus.setText("Đã xác nhận nhập kho");
            lblReceivingStatus.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-background-radius: 6; -fx-padding: 8 10; -fx-font-weight: bold;");
            btnConfirm.setDisable(true);
            showInfo("Nhập kho thành công", buildSuccessMessage(result));
            loadInTransitOrders();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API xác nhận nhập kho: " + e.getMessage());
        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API xác nhận nhập kho: " + e.getMessage());
        }
    }

    private void setupOrderList() {
        orderList.setItems(filteredOrders);
        orderList.setPlaceholder(new Label("Không có đơn IN_TRANSIT."));
        orderList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(PurchaseOrderRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(item.getOrderId() + "\n" + item.getSiteName() + "\n" + item.getSiteCode() + " | " + item.getCreatedDate());
                setStyle("-fx-padding: 10; -fx-font-size: 12px;");
            }
        });
    }

    private void setupFilters() {
        txtOrderFilter.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        dpCreatedDateFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cbSiteFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void setupReadonlyPanels() {
        txtReportPreview.setEditable(false);
        txtReportPreview.setWrapText(true);
        txtReceivingNotePreview.setEditable(false);
        txtReceivingNotePreview.setWrapText(true);
    }

    private void setupLineTable() {
        lineTable.setEditable(true);
        lineTable.setPlaceholder(new Label("Chọn một đơn đang vận chuyển để xem danh sách đối soát."));

        colMerchandiseCode.setCellValueFactory(data -> data.getValue().merchandiseCodeProperty());
        colMerchandiseName.setCellValueFactory(data -> data.getValue().merchandiseNameProperty());
        colOrderedQty.setCellValueFactory(data -> data.getValue().orderedQtyProperty().asObject());
        colReceivedQty.setCellValueFactory(data -> data.getValue().receivedQtyProperty().asObject());
        colDifferenceQty.setCellValueFactory(data -> data.getValue().differenceQtyProperty().asObject());
        colLineStatus.setCellValueFactory(data -> data.getValue().statusProperty());

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
            row.recalculate();
            clearReportIfNoDiscrepancy();
            updateDetailMetrics();
            lineTable.refresh();
        });
    }

    private void loadInTransitOrders() {
        try {
            allOrders.clear();
            allOrders.addAll(apiClient.getInTransitOrders()
                    .stream()
                    .map(row -> new PurchaseOrderRow(
                            row.getOrderId(),
                            row.getRequestCode(),
                            row.getSiteCode(),
                            row.getSiteName(),
                            row.getStatus(),
                            row.getCreatedDate()
                    ))
                    .toList());
            refreshSiteFilterOptions();
            applyFilters();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API danh sách PO: " + e.getMessage());
        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API danh sách PO: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String keyword = normalize(txtOrderFilter.getText()).toLowerCase(Locale.ROOT);
        LocalDate filterDate = dpCreatedDateFilter.getValue();
        String siteFilter = cbSiteFilter.getValue();

        filteredOrders.setAll(allOrders.stream()
                .filter(order -> matchesKeyword(order, keyword))
                .filter(order -> matchesDate(order, filterDate))
                .filter(order -> matchesSite(order, siteFilter))
                .toList());
        lblOrderCount.setText(filteredOrders.size() + " đơn");
    }

    private void refreshSiteFilterOptions() {
        String selectedSite = cbSiteFilter.getValue();
        ObservableList<String> siteOptions = FXCollections.observableArrayList(
                allOrders.stream()
                        .map(order -> order.getSiteCode() + " - " + order.getSiteName())
                        .distinct()
                        .sorted()
                        .toList()
        );
        cbSiteFilter.setItems(siteOptions);
        if (selectedSite != null && siteOptions.contains(selectedSite)) {
            cbSiteFilter.setValue(selectedSite);
        }
    }

    private boolean matchesKeyword(PurchaseOrderRow order, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }
        String haystack = normalize(order.getOrderId() + " " + order.getRequestCode() + " "
                + order.getSiteCode() + " " + order.getSiteName()).toLowerCase(Locale.ROOT);
        return haystack.contains(keyword);
    }

    private boolean matchesSite(PurchaseOrderRow order, String siteFilter) {
        if (siteFilter == null || siteFilter.isBlank()) {
            return true;
        }
        return siteFilter.equals(order.getSiteCode() + " - " + order.getSiteName());
    }

    private boolean matchesDate(PurchaseOrderRow order, LocalDate filterDate) {
        if (filterDate == null) {
            return true;
        }
        try {
            return LocalDate.parse(order.getCreatedDate()).equals(filterDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void handleOrderSelected(PurchaseOrderRow selected) {
        if (selected == null) {
            return;
        }

        selectedOrder = selected;
        discrepancyInput = null;
        receivingNoteInput = null;
        confirmed = false;
        lblReceivingStatus.setText("Chưa xác nhận nhập kho");
        lblReceivingStatus.setStyle("-fx-background-color: #fff7ed; -fx-text-fill: #c2410c; -fx-background-radius: 6; -fx-padding: 8 10; -fx-font-weight: bold;");
        btnConfirm.setDisable(false);
        renderReportPanel();
        renderReceivingNotePanel();
        loadReconciliationDetail(selected.getOrderId());
    }

    private void loadReconciliationDetail(String orderId) {
        try {
            ReconciliationDetailDTO detail = apiClient.getReconciliationDetail(orderId);
            lineTable.setItems(FXCollections.observableArrayList(
                    detail.getLines().stream().map(dto -> toLineRow(dto, detail)).toList()
            ));
            recalculateDiscrepancy();
            updateOrderPanel(detail);
            lblSelectedPo.setText("Đang chọn: " + detail.getOrderId() + " | " + detail.getSiteCode() + " - " + detail.getSiteName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showError("Lỗi kết nối", "Không gọi được API chi tiết đối soát: " + e.getMessage());
        } catch (IOException e) {
            showError("Lỗi kết nối", "Không gọi được API chi tiết đối soát: " + e.getMessage());
        }
    }

    private LineRow toLineRow(PartialOrderSelectionDTO dto, ReconciliationDetailDTO detail) {
        int orderedQty = dto.getOrderedQty() == null ? 0 : dto.getOrderedQty();
        int receivedQty = dto.getReceivedQty() == null ? orderedQty : dto.getReceivedQty();
        LineRow row = new LineRow(
                dto.getLineId(),
                dto.getMerchandiseCode(),
                dto.getMerchandiseName(),
                orderedQty,
                receivedQty,
                dto.getUnit()
        );
        row.recalculate();
        return row;
    }

    private void updateOrderPanel(ReconciliationDetailDTO detail) {
        lblDetailOrderId.setText(detail.getOrderId());
        lblDetailRequestCode.setText(detail.getRequestCode());
        lblDetailSupplier.setText(detail.getSiteName());
        lblDetailSite.setText(detail.getSiteCode());
        lblDetailStatus.setText(confirmed ? "RECEIVED" : detail.getStatus());
        updateDetailMetrics();
    }

    private void updateDetailMetrics() {
        int itemCount = lineTable.getItems() == null ? 0 : lineTable.getItems().size();
        long discrepancyCount = lineTable.getItems() == null ? 0 : lineTable.getItems().stream()
                .filter(LineRow::hasDiscrepancy)
                .count();

        lblDetailItemCount.setText(String.valueOf(itemCount));
        lblDetailDiscrepancyCount.setText(String.valueOf(discrepancyCount));
    }

    private void resetRightPanel() {
        lblSelectedPo.setText("Chưa chọn đơn hàng");
        lblDetailOrderId.setText("");
        lblDetailRequestCode.setText("");
        lblDetailSupplier.setText("");
        lblDetailSite.setText("");
        lblDetailStatus.setText("");
        lblDetailItemCount.setText("0");
        lblDetailDiscrepancyCount.setText("0");
        renderReportPanel();
        renderReceivingNotePanel();
        lblReceivingStatus.setText("Chưa xác nhận nhập kho");
        lblReceivingStatus.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-background-radius: 6; -fx-padding: 8 10; -fx-font-weight: bold;");
        btnConfirm.setDisable(true);
    }

    private void recalculateDiscrepancy() {
        if (lineTable.getItems() == null) {
            return;
        }
        lineTable.getItems().forEach(LineRow::recalculate);
        clearReportIfNoDiscrepancy();
        updateDetailMetrics();
        updateReportButtonVisibility();
        lineTable.refresh();
    }

    private void clearReportIfNoDiscrepancy() {
        if (!hasDiscrepancy()) {
            discrepancyInput = null;
            renderReportPanel();
        }
        updateReportButtonVisibility();
    }

    private void updateReportButtonVisibility() {
        boolean visible = hasDiscrepancy() && !confirmed;
        btnCreateReport.setVisible(visible);
        btnCreateReport.setManaged(visible);
    }

    private boolean hasDiscrepancy() {
        return lineTable.getItems() != null && lineTable.getItems().stream().anyMatch(LineRow::hasDiscrepancy);
    }

    private ReconciliationSubmitDTO buildSubmitDTO() {
        String reason = discrepancyInput == null ? "" : discrepancyInput.reason();
        String note = discrepancyInput == null ? receivingNoteInput.note() : discrepancyInput.note();
        String createdBy = discrepancyInput == null ? receivingNoteInput.carrier() : discrepancyInput.createdBy();

        return new ReconciliationSubmitDTO(
                lineTable.getItems().stream()
                        .map(line -> new ReceivedLineDTO(line.getLineId(), line.getReceivedQty()))
                        .toList(),
                reason,
                note,
                createdBy
        );
    }

    private Optional<DiscrepancyInput> showDiscrepancyDialog(DiscrepancyInput existing) {
        Dialog<DiscrepancyInput> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Lập biên bản sai lệch" : "Sửa biên bản sai lệch");
        dialog.setHeaderText("Nhập thông tin biên bản sai lệch cho đơn " + selectedOrder.getOrderId());

        ButtonType saveButtonType = new ButtonType("Lưu biên bản", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField createdByField = new TextField(existing == null ? "Nhân viên kho" : existing.createdBy());
        TextArea reasonArea = new TextArea(existing == null ? "" : existing.reason());
        reasonArea.setPromptText("Lý do sai lệch");
        reasonArea.setPrefRowCount(3);
        TextArea noteArea = new TextArea(existing == null ? "" : existing.note());
        noteArea.setPromptText("Ghi chú kiểm đếm, hướng xử lý...");
        noteArea.setPrefRowCount(3);

        TextArea summaryArea = new TextArea(buildDiscrepancySummary());
        summaryArea.setEditable(false);
        summaryArea.setWrapText(true);
        summaryArea.setPrefRowCount(4);

        GridPane form = dialogForm();
        form.add(new Label("Dòng sai lệch:"), 0, 0);
        form.add(summaryArea, 1, 0);
        form.add(new Label("Người lập:"), 0, 1);
        form.add(createdByField, 1, 1);
        form.add(new Label("Lý do:"), 0, 2);
        form.add(reasonArea, 1, 2);
        form.add(new Label("Ghi chú:"), 0, 3);
        form.add(noteArea, 1, 3);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(620.0);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(reasonArea.getText().isBlank());
        reasonArea.textProperty().addListener((observable, oldValue, newValue) -> saveButton.setDisable(newValue.isBlank()));

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                return new DiscrepancyInput(reasonArea.getText().trim(), noteArea.getText().trim(), normalizeText(createdByField.getText(), "Nhân viên kho"));
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private Optional<ReceivingNoteInput> showReceivingNoteDialog(ReceivingNoteInput existing) {
        Dialog<ReceivingNoteInput> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thông tin nhập kho" : "Sửa thông tin nhập kho");
        dialog.setHeaderText("Nhập ghi chú và người vận chuyển trước khi xác nhận nhập kho.");

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField carrierField = new TextField(existing == null ? "" : existing.carrier());
        carrierField.setPromptText("Người vận chuyển");
        TextArea noteArea = new TextArea(existing == null ? "" : existing.note());
        noteArea.setPromptText("Ghi chú sau xác nhận nhập kho");
        noteArea.setPrefRowCount(4);

        GridPane form = dialogForm();
        form.add(new Label("Người vận chuyển:"), 0, 0);
        form.add(carrierField, 1, 0);
        form.add(new Label("Ghi chú:"), 0, 1);
        form.add(noteArea, 1, 1);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(520.0);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(carrierField.getText().isBlank());
        carrierField.textProperty().addListener((observable, oldValue, newValue) -> saveButton.setDisable(newValue.isBlank()));

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                return new ReceivingNoteInput(noteArea.getText().trim(), carrierField.getText().trim());
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private GridPane dialogForm() {
        GridPane form = new GridPane();
        form.setHgap(12.0);
        form.setVgap(10.0);
        return form;
    }

    private void renderReportPanel() {
        if (discrepancyInput == null) {
            txtReportPreview.clear();
            btnEditReport.setVisible(false);
            btnEditReport.setManaged(false);
            return;
        }
        txtReportPreview.setText("Lý do: " + discrepancyInput.reason()
                + "\nNgười lập: " + discrepancyInput.createdBy()
                + "\nGhi chú: " + normalizeText(discrepancyInput.note(), "Không có")
                + "\n\n" + buildDiscrepancySummary());
        btnEditReport.setVisible(true);
        btnEditReport.setManaged(true);
    }

    private void renderReceivingNotePanel() {
        if (receivingNoteInput == null) {
            txtReceivingNotePreview.clear();
            btnEditReceivingNote.setVisible(false);
            btnEditReceivingNote.setManaged(false);
            return;
        }
        txtReceivingNotePreview.setText("Người vận chuyển: " + receivingNoteInput.carrier()
                + "\nGhi chú: " + normalizeText(receivingNoteInput.note(), "Không có"));
        btnEditReceivingNote.setVisible(true);
        btnEditReceivingNote.setManaged(true);
    }

    private String buildDiscrepancySummary() {
        StringBuilder builder = new StringBuilder();
        for (LineRow line : lineTable.getItems()) {
            if (!line.hasDiscrepancy()) {
                continue;
            }
            builder.append(line.getMerchandiseCode())
                    .append(" - ")
                    .append(line.getMerchandiseName())
                    .append(": SL đặt ")
                    .append(line.getOrderedQty())
                    .append(", SL nhận ")
                    .append(line.getReceivedQty())
                    .append(", lệch SL ")
                    .append(line.getDifferenceQty())
                    .append("\n");
        }
        return builder.toString();
    }

    private String buildSuccessMessage(ReconciliationResultDTO result) {
        StringBuilder builder = new StringBuilder();
        builder.append(result.getMessage()).append("\n");
        builder.append("Mã PO: ").append(result.getOrderId()).append("\n");
        builder.append("Trạng thái mới: ").append(result.getStatus()).append("\n");
        if (result.isHasDiscrepancy() && result.getDiscrepancyReportIds() != null && !result.getDiscrepancyReportIds().isEmpty()) {
            builder.append("Biên bản sai lệch:\n");
            result.getDiscrepancyReportIds().forEach(reportId -> builder.append("- ").append(reportId).append("\n"));
        }
        return builder.toString();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
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
        private final String orderId;
        private final String requestCode;
        private final String siteCode;
        private final String siteName;
        private final String status;
        private final String createdDate;

        public PurchaseOrderRow(String orderId, String requestCode, String siteCode, String siteName, String status, String createdDate) {
            this.orderId = orderId;
            this.requestCode = requestCode;
            this.siteCode = siteCode;
            this.siteName = siteName;
            this.status = status;
            this.createdDate = createdDate;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getRequestCode() {
            return requestCode;
        }

        public String getSiteCode() {
            return siteCode;
        }

        public String getSiteName() {
            return siteName;
        }

        public String getStatus() {
            return status;
        }

        public String getCreatedDate() {
            return createdDate;
        }
    }

    public class LineRow {
        private final SimpleLongProperty lineId;
        private final SimpleStringProperty merchandiseCode;
        private final SimpleStringProperty merchandiseName;
        private final SimpleIntegerProperty orderedQty;
        private final SimpleIntegerProperty receivedQty;
        private final SimpleIntegerProperty differenceQty;
        private final SimpleStringProperty status;
        private final SimpleStringProperty unit;

        public LineRow(Long lineId, String merchandiseCode, String merchandiseName,
                       Integer orderedQty, Integer receivedQty, String unit) {
            this.lineId = new SimpleLongProperty(lineId == null ? 0L : lineId);
            this.merchandiseCode = new SimpleStringProperty(merchandiseCode == null ? "" : merchandiseCode);
            this.merchandiseName = new SimpleStringProperty(merchandiseName == null ? "" : merchandiseName);
            this.orderedQty = new SimpleIntegerProperty(orderedQty == null ? 0 : orderedQty);
            this.receivedQty = new SimpleIntegerProperty(receivedQty == null ? 0 : receivedQty);
            this.differenceQty = new SimpleIntegerProperty(0);
            this.status = new SimpleStringProperty("Hợp lệ");
            this.unit = new SimpleStringProperty(unit == null ? "" : unit);
        }

        public void recalculate() {
            int diffQty = orderedQty.get() - receivedQty.get();
            differenceQty.set(diffQty);
            status.set(diffQty == 0 ? "Hợp lệ" : "Sai lệch");
        }

        public boolean hasDiscrepancy() {
            return differenceQty.get() != 0;
        }

        public Long getLineId() {
            return lineId.get();
        }

        public String getMerchandiseCode() {
            return merchandiseCode.get();
        }

        public String getMerchandiseName() {
            return merchandiseName.get();
        }

        public int getOrderedQty() {
            return orderedQty.get();
        }

        public int getReceivedQty() {
            return receivedQty.get();
        }

        public void setReceivedQty(Integer value) {
            receivedQty.set(value == null ? 0 : value);
        }

        public int getDifferenceQty() {
            return differenceQty.get();
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

        public SimpleStringProperty statusProperty() {
            return status;
        }

        public SimpleStringProperty unitProperty() {
            return unit;
        }
    }

    private record DiscrepancyInput(String reason, String note, String createdBy) {
    }

    private record ReceivingNoteInput(String note, String carrier) {
    }
}
