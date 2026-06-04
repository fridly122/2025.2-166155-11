package itss.group11.controller.chung;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StageManager {
    // Biáº¿n static lÆ°u trá»¯ cá»­a sá»• chÃ­nh cá»§a pháº§n má»m
    private static Stage primaryStage;

    /**
     * HÃ m nÃ y chá»‰ Ä‘Æ°á»£c gá»i DUY NHáº¤T 1 Láº¦N khi pháº§n má»m vá»«a khá»Ÿi Ä‘á»™ng táº¡i App.java
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * HÃ m dÃ¹ng chung Ä‘á»ƒ chuyá»ƒn Ä‘á»•i mÃ n hÃ¬nh (Load file FXML)
     * * @param fxmlPath ÄÆ°á»ng dáº«n tuyá»‡t Ä‘á»‘i tÃ­nh tá»« thÆ° má»¥c resources (VÃ­ dá»¥: "/itss/group11/view/chung/dashboard.fxml")
     * @param title    TiÃªu Ä‘á» hiá»ƒn thá»‹ trÃªn cá»­a sá»•
     * @return Tráº£ vá» Controller cá»§a mÃ n hÃ¬nh má»›i
     */
    public static Object switchScene(String fxmlPath, String title) {
        try {
            // Láº¥y URL cá»§a file FXML tá»« tÃ i nguyÃªn há»‡ thá»‘ng
            URL fxmlUrl = StageManager.class.getResource(fxmlPath);
            
            // Kiá»ƒm tra náº¿u khÃ´ng tÃ¬m tháº¥y file (trÃ¡nh lá»—i Location is not set)
            if (fxmlUrl == null) {
                throw new IllegalArgumentException("KHONG TIM THAY FILE FXML! Hay kiem tra lai chinh xac duong dan: " + fxmlPath);
            }

            // Táº£i file giao diá»‡n
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            // Táº¡o Scene má»›i vÃ  náº¡p vÃ o cá»­a sá»• chÃ­nh
            Scene scene = new Scene(root);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            
            // CÄƒn giá»¯a cá»­a sá»• trÃªn mÃ n hÃ¬nh mÃ¡y tÃ­nh
            primaryStage.centerOnScreen();
            primaryStage.show();
            
            // Tráº£ vá» Controller cá»§a mÃ n hÃ¬nh vá»«a load
            return loader.getController();
            
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("LOI NGHIEM TRONG tai StageManager: Khong the chuyen man hinh!");
            e.printStackTrace();
            return null;
        }
    }
}

