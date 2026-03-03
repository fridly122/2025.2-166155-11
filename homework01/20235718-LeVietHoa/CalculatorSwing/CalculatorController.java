public class CalculatorController {

    private CalculatorModel model;
    private CalculatorView view;

    public CalculatorController(CalculatorModel model, CalculatorView view) {
        this.model = model;
        this.view = view;

        view.getAddButton().addActionListener(e -> {
            try {
                int a = Integer.parseInt(view.getNum1().getText());
                int b = Integer.parseInt(view.getNum2().getText());
                int result = model.add(a, b);
                view.getResultLabel().setText("Result: " + result);
            } catch (NumberFormatException ex) {
                view.getResultLabel().setText("Invalid input!");
            }
        });
    }
}