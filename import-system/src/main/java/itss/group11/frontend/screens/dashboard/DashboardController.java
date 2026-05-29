package itss.group11.frontend.screens.dashboard;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class DashboardController {

    @FXML private AnchorPane contentArea;

    private static DashboardController instance;

    public static DashboardController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        // LƯU Ý: Sửa đường dẫn cho đúng với folder thực tế trong src/main/resources
        // Kiểm tra kỹ xem file có nằm trong "itss/group11/frontend/screens/..." không nhé
        loadSubScreen("/itss/group11/allocationList/allocationList.fxml");
    }

    public Object loadSubScreen(String fxmlPath) {
        try {
            // 1. Kiểm tra xem file có tồn tại không trước khi load
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("LOI: Khong tim thay file FXML tai duong dan: " + fxmlPath);
                System.err.println("Hay kiem tra lai cau truc folder trong src/main/resources!");
                return null;
            }

            // 2. Load file
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            // 3. Cấu hình layout
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

    // Các hàm bắt sự kiện menu - Hãy cập nhật lại đường dẫn cho chuẩn
    @FXML private void handleNavCreateRequest() {
        loadSubScreen("/itss/group11/orderRequestCreate/orderRequestCreate.fxml");
    }

    @FXML private void handleNavClassification() {
        loadSubScreen("/itss/group11/siteClassification/siteClassification.fxml");
    }

    @FXML private void handleNavShipping() {
        loadSubScreen("/itss/group11/siteShippingManage/siteShippingManage.fxml");
    }

    @FXML private void handleNavAllocation() {
        loadSubScreen("/itss/group11/allocationList/allocationList.fxml");
    }

    @FXML private void handleNavReconciliation() {
        loadSubScreen("/itss/group11/orderReconciliation/orderReconciliation.fxml");
    }
}