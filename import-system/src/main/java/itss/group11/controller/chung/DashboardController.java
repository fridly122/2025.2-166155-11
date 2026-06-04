package itss.group11.controller.chung;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import itss.group11.controller.chung.AppFeature;
import itss.group11.controller.chung.LoginSession;
import itss.group11.controller.chung.UserRole;
import itss.group11.controller.chung.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

public class DashboardController {

    private static final String NAV_BASE_STYLE =
            "-fx-background-color: transparent; " +
            "-fx-text-fill: white; " +
            "-fx-alignment: LEFT; " +
            "-fx-font-weight: normal; " +
            "-fx-cursor: hand;";

    private static final String NAV_HOVER_STYLE =
            "-fx-background-color: #3b536b; " +
            "-fx-text-fill: white; " +
            "-fx-alignment: LEFT; " +
            "-fx-font-weight: normal; " +
            "-fx-cursor: hand;";

    private static final String NAV_ACTIVE_STYLE =
            "-fx-background-color: #34495e; " +
            "-fx-text-fill: white; " +
            "-fx-alignment: LEFT; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;";

    private static final String NAV_DISABLED_STYLE =
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #7f8c8d; " +
            "-fx-alignment: LEFT; " +
            "-fx-font-weight: normal; " +
            "-fx-cursor: default; " +
            "-fx-opacity: 0.65;";

    @FXML
    private AnchorPane contentArea;

    @FXML
    private Label lblCurrentRole;

    @FXML
    private Button btnCreateRequest;

    @FXML
    private Button btnClassification;

    @FXML
    private Button btnInventory;

    @FXML
    private Button btnShipping;

    @FXML
    private Button btnAllocation;

    @FXML
    private Button btnReconciliation;

    private Button activeButton;

    private static DashboardController instance;

    public static DashboardController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;

        if (!LoginSession.isAuthenticated()) {
            StageManager.switchScene(
                    "/itss/group11/view/chung/login.fxml",
                    "ÄÄƒng nháº­p - Há»‡ thá»‘ng Äáº·t hÃ ng Nháº­p kháº©u"
            );
            return;
        }

        UserRole role = LoginSession.getRole();
        lblCurrentRole.setText("Vai trÃ²: " + role.getDisplayName());
        setupSidebarPermissions(role);
        setupSidebarHover();
        navigateToDefaultFeature(role);
    }

    public Object loadSubScreen(String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("LOI: Khong tim thay file FXML tai duong dan: " + fxmlPath);
                return null;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

            contentArea.getChildren().setAll(root);

            return loader.getController();
        } catch (IOException e) {
            System.err.println("LOI I/O khi nap man hinh: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void handleNavCreateRequest() {
        navigateTo(AppFeature.CREATE_REQUEST, btnCreateRequest, "/itss/group11/view/uc1/orderRequestCreate.fxml");
    }

    @FXML
    private void handleNavClassification() {
        navigateTo(AppFeature.CLASSIFICATION, btnClassification, "/itss/group11/view/uc2/siteClassification.fxml");
    }

    @FXML
    private void handleNavInventory() {
        navigateTo(AppFeature.INVENTORY, btnInventory, "/itss/group11/view/uc3/siteInventoryManage.fxml");
    }

    @FXML
    private void handleNavShipping() {
        navigateTo(AppFeature.SHIPPING, btnShipping, "/itss/group11/view/uc4/siteShippingManage.fxml");
    }

    @FXML
    private void handleNavAllocation() {
        navigateTo(AppFeature.ALLOCATION, btnAllocation, "/itss/group11/view/uc5/allocationList.fxml");
    }

    @FXML
    private void handleNavReconciliation() {
        navigateTo(AppFeature.RECONCILIATION, btnReconciliation, "/itss/group11/view/uc6/orderReconciliation.fxml");
    }

    @FXML
    private void handleLogout() {
        LoginSession.clear();
        StageManager.switchScene(
                "/itss/group11/view/chung/login.fxml",
                "ÄÄƒng nháº­p - Há»‡ thá»‘ng Äáº·t hÃ ng Nháº­p kháº©u"
        );
    }

    private void navigateTo(AppFeature feature, Button button, String fxmlPath) {
        UserRole role = LoginSession.getRole();
        if (role == null || !role.canAccess(feature)) {
            showAccessDenied();
            return;
        }

        Object controller = loadSubScreen(fxmlPath);
        if (controller != null) {
            setActiveButton(button);
        }
    }

    private void setupSidebarHover() {
        for (Button button : getNavButtons()) {
            if (button.isDisabled()) {
                button.setStyle(NAV_DISABLED_STYLE);
                continue;
            }

            button.setStyle(button == activeButton ? NAV_ACTIVE_STYLE : NAV_BASE_STYLE);

            button.setOnMouseEntered(event -> {
                if (button != activeButton) {
                    button.setStyle(NAV_HOVER_STYLE);
                }
            });

            button.setOnMouseExited(event -> {
                if (button != activeButton) {
                    button.setStyle(NAV_BASE_STYLE);
                }
            });
        }
    }

    private void setActiveButton(Button selectedButton) {
        activeButton = selectedButton;
        for (Button button : getNavButtons()) {
            if (button.isDisabled()) {
                button.setStyle(NAV_DISABLED_STYLE);
                continue;
            }

            button.setStyle(button == activeButton ? NAV_ACTIVE_STYLE : NAV_BASE_STYLE);
        }
    }

    private void setupSidebarPermissions(UserRole role) {
        setFeaturePermission(btnCreateRequest, role.canAccess(AppFeature.CREATE_REQUEST));
        setFeaturePermission(btnClassification, role.canAccess(AppFeature.CLASSIFICATION));
        setFeaturePermission(btnInventory, role.canAccess(AppFeature.INVENTORY));
        setFeaturePermission(btnShipping, role.canAccess(AppFeature.SHIPPING));
        setFeaturePermission(btnAllocation, role.canAccess(AppFeature.ALLOCATION));
        setFeaturePermission(btnReconciliation, role.canAccess(AppFeature.RECONCILIATION));
    }

    private void setFeaturePermission(Button button, boolean allowed) {
        button.setDisable(!allowed);
        button.setVisible(allowed);
        button.setManaged(allowed);
        button.setStyle(allowed ? NAV_BASE_STYLE : NAV_DISABLED_STYLE);
    }

    private void navigateToDefaultFeature(UserRole role) {
        switch (role.getDefaultFeature()) {
            case CREATE_REQUEST -> handleNavCreateRequest();
            case CLASSIFICATION -> handleNavClassification();
            case INVENTORY -> handleNavInventory();
            case SHIPPING -> handleNavShipping();
            case ALLOCATION -> handleNavAllocation();
            case RECONCILIATION -> handleNavReconciliation();
        }
    }

    private void showAccessDenied() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("KhÃ´ng cÃ³ quyá»n truy cáº­p");
        alert.setHeaderText("KhÃ´ng cÃ³ quyá»n truy cáº­p");
        alert.setContentText("Vai trÃ² hiá»‡n táº¡i khÃ´ng Ä‘Æ°á»£c phÃ©p sá»­ dá»¥ng chá»©c nÄƒng nÃ y.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private List<Button> getNavButtons() {
        return List.of(
                btnCreateRequest,
                btnClassification,
                btnInventory,
                btnShipping,
                btnAllocation,
                btnReconciliation
        );
    }
}


