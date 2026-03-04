public class CalculatorModel {

    private double result;

    public void calculate(double a, double b, String operator) {

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
                if (b == 0) {
                    throw new ArithmeticException("Cannot divide by zero");
                }
                result = a / b;
                break;
        }
    }

    public double getResult() {
        return result;
    }
}