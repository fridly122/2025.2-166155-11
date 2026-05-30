package itss.group11.frontend.screens.dashboard;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import itss.group11.frontend.stage.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

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

    @FXML
    private AnchorPane contentArea;

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
        setupSidebarHover();
        navigateTo(btnAllocation, "/itss/group11/allocationList/allocationList.fxml");
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
        navigateTo(btnCreateRequest, "/itss/group11/orderRequestCreate/orderRequestCreate.fxml");
    }

    @FXML
    private void handleNavClassification() {
        navigateTo(btnClassification, "/itss/group11/siteClassification/siteClassification.fxml");
    }

    @FXML
    private void handleNavInventory() {
        navigateTo(btnInventory, "/itss/group11/siteInventoryManage/siteInventoryManage.fxml");
    }

    @FXML
    private void handleNavShipping() {
        navigateTo(btnShipping, "/itss/group11/siteShippingManage/siteShippingManage.fxml");
    }

    @FXML
    private void handleNavAllocation() {
        navigateTo(btnAllocation, "/itss/group11/allocationList/allocationList.fxml");
    }

    @FXML
    private void handleNavReconciliation() {
        navigateTo(btnReconciliation, "/itss/group11/orderReconciliation/orderReconciliation.fxml");
    }

    @FXML
    private void handleLogout() {
        StageManager.switchScene(
                "/itss/group11/login/login.fxml",
                "Đăng nhập - Hệ thống Đặt hàng Nhập khẩu"
        );
    }

    private void navigateTo(Button button, String fxmlPath) {
        Object controller = loadSubScreen(fxmlPath);
        if (controller != null) {
            setActiveButton(button);
        }
    }

    private void setupSidebarHover() {
        for (Button button : getNavButtons()) {
            button.setStyle(NAV_BASE_STYLE);

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
            button.setStyle(button == activeButton ? NAV_ACTIVE_STYLE : NAV_BASE_STYLE);
        }
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
