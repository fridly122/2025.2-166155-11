package itss.group11.frontend;

import itss.group11.ImportSystemBackendApplication;
import itss.group11.frontend.stage.StageManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * JavaFX entry point. It also starts the embedded Spring Boot backend so
 * allocationList can call http://localhost:8080 from inside the same desktop app.
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
                "/itss/group11/login/login.fxml",
                "Dang nhap - He thong Dat hang Nhap khau"
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
