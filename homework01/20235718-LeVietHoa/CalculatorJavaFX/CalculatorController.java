import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CalculatorController {

    @FXML
    private TextField num1;

    @FXML
    private TextField num2;

    @FXML
    private Label result;

    @FXML
    public void handleAdd() {
        try {
            double a = Double.parseDouble(num1.getText());
            double b = Double.parseDouble(num2.getText());
            double sum = a + b;

            result.setText("Result: " + sum);
        } catch (Exception e) {
            result.setText("Invalid input!");
        }
    }
}