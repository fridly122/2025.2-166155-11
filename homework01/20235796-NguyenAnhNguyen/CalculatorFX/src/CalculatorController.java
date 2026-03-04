import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class CalculatorController {

    @FXML
    private TextField firstNumber;

    @FXML
    private TextField secondNumber;

    @FXML
    private TextField resultField;

    @FXML
    private void handleAdd() {
        calculate("+");
    }

    @FXML
    private void handleSub() {
        calculate("-");
    }

    @FXML
    private void handleMul() {
        calculate("*");
    }

    @FXML
    private void handleDiv() {
        calculate("/");
    }

    private void calculate(String operator) {
        try {
            double a = Double.parseDouble(firstNumber.getText());
            double b = Double.parseDouble(secondNumber.getText());
            double result = 0;

            switch (operator) {
                case "+":
                    result = a + b;
                    break;
                case "-":
                    result = a - b;
                    break;
                case "*":
                    result = a * b;
                    break;
                case "/":
                    result = a / b;
                    break;
            }

            resultField.setText(String.valueOf(result));

        } catch (NumberFormatException e) {
            resultField.setText("Invalid input");
        }
    }
}