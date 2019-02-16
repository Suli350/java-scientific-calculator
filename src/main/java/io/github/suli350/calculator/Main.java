package io.github.suli350.calculator;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            // Command line mode: java -jar calculator.jar "2*sin(30)+3!"
            Calculator calculator = new Calculator();
            try {
                System.out.println(Formatter.format(calculator.evaluate(String.join(" ", args))));
            } catch (CalculatorException e) {
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
            }
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // default look and feel is fine
            }
            new CalculatorFrame().setVisible(true);
        });
    }
}
