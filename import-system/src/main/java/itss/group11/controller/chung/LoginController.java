package itss.group11.controller.chung;

import itss.group11.controller.chung.LoginSession;
import itss.group11.controller.chung.UserRole;
import itss.group11.controller.chung.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    private static final String MOCK_PASSWORD = "123456";

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMessage;

    @FXML
    public void initialize() {
        lblMessage.setText("");
        LoginSession.clear();
        txtUsername.setText(UserRole.SALES.getUsername());
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String password = txtPassword.getText() == null ? "" : txtPassword.getText();

        if (username.isBlank() || password.isBlank()) {
            showMessage("Vui lÃ²ng nháº­p Ä‘áº§y Ä‘á»§ tÃªn Ä‘Äƒng nháº­p vÃ  máº­t kháº©u.");
            return;
        }

        UserRole role = UserRole.findByUsername(username).orElse(null);
        if (role == null || !MOCK_PASSWORD.equals(password)) {
            txtPassword.clear();
            showMessage("TÃªn Ä‘Äƒng nháº­p hoáº·c máº­t kháº©u khÃ´ng Ä‘Ãºng.");
            return;
        }

        LoginSession.login(role.getUsername(), role);

        StageManager.switchScene(
                "/itss/group11/view/chung/dashboard.fxml",
                "Há»‡ thá»‘ng Äáº·t hÃ ng Nháº­p kháº©u"
        );
    }

    private void showMessage(String message) {
        lblMessage.setText(message);
    }
}


