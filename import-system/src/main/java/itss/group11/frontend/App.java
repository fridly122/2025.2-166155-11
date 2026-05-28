package itss.group11.frontend;

import itss.group11.frontend.stage.StageManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX App - Điểm khởi chạy chính của phần mềm hệ thống Nhóm 11
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Giao quyền quản lý cửa sổ chính (Primary Stage) cho hệ thống StageManager dùng chung
        StageManager.setPrimaryStage(primaryStage);
        
        // 2. Thiết lập kích thước tối thiểu để giao diện không bị co rúm, vỡ layout
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // 3. Khởi chạy màn hình đầu tiên khi vừa bật App
        // Ở đây chúng ta trỏ thẳng vào màn hình chức năng của bạn (UC005) để tiện test code
        StageManager.switchScene(
            "/itss/group11/dashboard/dashboard.fxml", 
            "Hệ thống Đặt hàng Nhập khẩu - Nhóm 11"
        );
    }

    public static void main(String[] args) {
        // Kích hoạt ứng dụng JavaFX
        launch(args);
    }
}