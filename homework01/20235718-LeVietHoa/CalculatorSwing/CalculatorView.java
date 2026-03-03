import java.awt.*;
import javax.swing.*;

public class CalculatorView extends JFrame {

    private JTextField num1 = new JTextField(5);
    private JTextField num2 = new JTextField(5);
    private JButton addButton = new JButton("Add");
    private JLabel resultLabel = new JLabel("Result:");

    public CalculatorView() {
        setTitle("Calculator MVC");
        setSize(300,150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        add(num1);
        add(num2);
        add(addButton);
        add(resultLabel);
    }

    public JTextField getNum1() { return num1; }
    public JTextField getNum2() { return num2; }
    public JButton getAddButton() { return addButton; }
    public JLabel getResultLabel() { return resultLabel; }
}