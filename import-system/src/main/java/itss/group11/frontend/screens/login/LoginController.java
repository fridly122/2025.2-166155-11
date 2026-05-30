package itss.group11.frontend.screens.login;

import itss.group11.frontend.stage.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    private static final String EMPLOYEE_USERNAME = "employee";
    private static final String EMPLOYEE_PASSWORD = "123456";

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMessage;

    @FXML
    public void initialize() {
        lblMessage.setText("");
        txtUsername.setText(EMPLOYEE_USERNAME);
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String password = txtPassword.getText() == null ? "" : txtPassword.getText();

        if (username.isBlank() || password.isBlank()) {
            showMessage("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        if (!EMPLOYEE_USERNAME.equals(username) || !EMPLOYEE_PASSWORD.equals(password)) {
            txtPassword.clear();
            showMessage("Tên đăng nhập hoặc mật khẩu không đúng.");
            return;
        }

        StageManager.switchScene(
                "/itss/group11/dashboard/dashboard.fxml",
                "Hệ thống Đặt hàng Nhập khẩu"
        );
    }

    private void showMessage(String message) {
        lblMessage.setText(message);
    }
}
