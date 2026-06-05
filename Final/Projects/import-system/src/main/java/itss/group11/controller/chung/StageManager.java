package itss.group11.controller.chung;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StageManager {
    // Biến static lưu trữ cửa sổ chính của phần mềm
    private static Stage primaryStage;

    /**
     * Hàm này chỉ được gọi DUY NHẤT 1 LẦN khi phần mềm vừa khởi động tại App.java
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Hàm dùng chung để chuyển đổi màn hình (Load file FXML)
     * * @param fxmlPath Đường dẫn tuyệt đối tính từ thư mục resources (Ví dụ: "/itss/group11/view/chung/dashboard.fxml")
     * @param title    Tiêu đề hiển thị trên cửa sổ
     * @return Trả về Controller của màn hình mới
     */
    public static Object switchScene(String fxmlPath, String title) {
        try {
            // Lấy URL của file FXML từ tài nguyên hệ thống
            URL fxmlUrl = StageManager.class.getResource(fxmlPath);
            
            // Kiểm tra nếu không tìm thấy file (tránh lỗi Location is not set)
            if (fxmlUrl == null) {
                throw new IllegalArgumentException("KHONG TIM THAY FILE FXML! Hay kiem tra lai chinh xac duong dan: " + fxmlPath);
            }

            // Tải file giao diện
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            // Tạo Scene mới và nạp vào cửa sổ chính
            Scene scene = new Scene(root);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            
            // Căn giữa cửa sổ trên màn hình máy tính
            primaryStage.centerOnScreen();
            primaryStage.show();
            
            // Trả về Controller của màn hình vừa load
            return loader.getController();
            
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("LOI NGHIEM TRONG tai StageManager: Khong the chuyen man hinh!");
            e.printStackTrace();
            return null;
        }
    }
}

