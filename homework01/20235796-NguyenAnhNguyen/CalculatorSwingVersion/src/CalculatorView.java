import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CalculatorView extends JFrame {

    private JTextField firstNumber = new JTextField(10);
    private JTextField secondNumber = new JTextField(10);
    private JTextField resultField = new JTextField(10);

    private JButton addBtn = new JButton("+");
    private JButton subBtn = new JButton("-");
    private JButton mulBtn = new JButton("*");
    private JButton divBtn = new JButton("/");

    public CalculatorView() {

        JPanel panel = new JPanel();

        this.setSize(400, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        resultField.setEditable(false);

        panel.add(firstNumber);
        panel.add(secondNumber);
        panel.add(addBtn);
        panel.add(subBtn);
        panel.add(mulBtn);
        panel.add(divBtn);
        panel.add(resultField);

        this.add(panel);

        // Set action command cho từng nút
        addBtn.setActionCommand("+");
        subBtn.setActionCommand("-");
        mulBtn.setActionCommand("*");
        divBtn.setActionCommand("/");
    }

    public double getFirstNumber() {
        return Double.parseDouble(firstNumber.getText());
    }

    public double getSecondNumber() {
        return Double.parseDouble(secondNumber.getText());
    }

    public void setResult(double result) {
        resultField.setText(Double.toString(result));
    }

    public void addCalculatorListener(ActionListener listener) {
        addBtn.addActionListener(listener);
        subBtn.addActionListener(listener);
        mulBtn.addActionListener(listener);
        divBtn.addActionListener(listener);
    }

    public void displayError(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}