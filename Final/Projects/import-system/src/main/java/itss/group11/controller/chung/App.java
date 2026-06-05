package itss.group11.controller.chung;

import itss.group11.ImportSystemBackendApplication;
import itss.group11.controller.chung.StageManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * JavaFX entry point. It also starts the embedded Spring Boot backend so
 * JavaFX screens call the embedded backend from inside the same desktop app.
 */
public class App extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = ImportSystemBackendApplication.run();
    }

    @Override
    public void start(Stage primaryStage) {
        StageManager.setPrimaryStage(primaryStage);

        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        StageManager.switchScene(
                "/itss/group11/view/chung/login.fxml",
                "Đăng nhập - Hệ thống Đặt hàng Nhập khẩu"
        );
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


