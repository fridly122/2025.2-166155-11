import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CalculatorController {

    private CalculatorView view;
    private CalculatorModel model;

    public CalculatorController(CalculatorView view, CalculatorModel model) {
        this.view = view;
        this.model = model;

        this.view.addCalculatorListener(new ButtonListener());
    }

    class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            try {
                double a = view.getFirstNumber();
                double b = view.getSecondNumber();

                String operator = e.getActionCommand();

                model.calculate(a, b, operator);

                view.setResult(model.getResult());

            } catch (Exception ex) {
                view.displayError("Invalid input!");
            }
        }
    }
}
