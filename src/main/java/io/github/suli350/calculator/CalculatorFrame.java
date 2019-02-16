package io.github.suli350.calculator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Swing front end. All maths lives in {@link Calculator}. */
public class CalculatorFrame extends JFrame {

    private static final Color OPERATOR = new Color(0xFF9F0A);
    private static final Color FUNCTION = new Color(0x3A3A3C);
    private static final Color DIGIT = new Color(0x505050);
    private static final Color BACKGROUND = new Color(0x1C1C1E);

    private final Calculator calculator = new Calculator();
    private final JTextField input = new JTextField();
    private final JLabel preview = new JLabel(" ");
    private final JLabel status = new JLabel();
    private final DefaultListModel<String> historyModel = new DefaultListModel<>();
    private boolean justEvaluated;

    public CalculatorFrame() {
        super("Scientific Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout(8, 8));

        add(buildDisplay(), BorderLayout.NORTH);
        add(buildKeypad(), BorderLayout.CENTER);
        add(buildHistory(), BorderLayout.EAST);
        bindKeys();
        updateStatus();

        pack();
        setMinimumSize(new Dimension(620, 520));
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ layout
    private JComponent buildDisplay() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        status.setForeground(Color.LIGHT_GRAY);
        status.setFont(status.getFont().deriveFont(12f));

        input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 28));
        input.setHorizontalAlignment(SwingConstants.RIGHT);
        input.setBackground(BACKGROUND);
        input.setForeground(Color.WHITE);
        input.setCaretColor(Color.WHITE);
        input.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        input.addActionListener(e -> evaluate());
        input.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePreview(); }
            public void removeUpdate(DocumentEvent e) { updatePreview(); }
            public void changedUpdate(DocumentEvent e) { updatePreview(); }
        });

        preview.setHorizontalAlignment(SwingConstants.RIGHT);
        preview.setForeground(Color.GRAY);
        preview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));

        panel.add(status, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        panel.add(preview, BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildKeypad() {
        String[][] rows = {
            {"DEG", "MC", "MR", "M+", "M-"},
            {"sin", "cos", "tan", "(", ")"},
            {"asin", "acos", "atan", "^", "√"},
            {"ln", "log", "!", "π", "e"},
            {"7", "8", "9", "÷", "C"},
            {"4", "5", "6", "×", "⌫"},
            {"1", "2", "3", "−", "ans"},
            {"0", ".", "%", "+", "="},
        };
        JPanel grid = new JPanel(new GridLayout(rows.length, 5, 6, 6));
        grid.setBackground(BACKGROUND);
        grid.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
        for (String[] row : rows) {
            for (String label : row) {
                grid.add(makeButton(label));
            }
        }
        return grid;
    }

    private JButton makeButton(String label) {
        JButton b = new JButton(label);
        b.setFocusable(false);
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        if ("0123456789.".contains(label)) {
            b.setBackground(DIGIT);
        } else if ("÷×−+=".contains(label)) {
            b.setBackground(OPERATOR);
        } else {
            b.setBackground(FUNCTION);
        }
        b.addActionListener(e -> onButton(label));
        return b;
    }

    private JComponent buildHistory() {
        JList<String> list = new JList<>(historyModel);
        list.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        list.setToolTipText("Double-click to reuse an expression");
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && list.getSelectedValue() != null) {
                    String entry = list.getSelectedValue();
                    input.setText(entry.substring(0, entry.lastIndexOf(" = ")));
                    justEvaluated = false;
                }
            }
        });
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
        panel.setBackground(BACKGROUND);
        JLabel title = new JLabel("History");
        title.setForeground(Color.LIGHT_GRAY);
        JButton clear = new JButton("Clear history");
        clear.setFocusable(false);
        clear.addActionListener(e -> {
            calculator.clearHistory();
            historyModel.clear();
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(210, 0));
        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(clear, BorderLayout.SOUTH);
        return panel;
    }

    private void bindKeys() {
        input.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear");
        input.getActionMap().put("clear", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onButton("C");
            }
        });
    }

    // ------------------------------------------------------------------ behaviour
    void onButton(String label) {
        switch (label) {
            case "=": evaluate(); break;
            case "C": input.setText(""); preview.setText(" "); break;
            case "⌫": backspace(); break;
            case "DEG":
            case "RAD": calculator.toggleAngleMode(); updateStatus(); updatePreview(); break;
            case "MC": calculator.memoryClear(); updateStatus(); break;
            case "MR": insert(Formatter.format(calculator.memoryRecall())); break;
            case "M+": memory(true); break;
            case "M-": memory(false); break;
            case "sin": case "cos": case "tan": case "asin": case "acos": case "atan":
            case "ln": case "log":
                insert(label + "("); break;
            case "√": insert("√("); break;
            case "π": insert("π"); break;
            default: insert(label);
        }
        input.requestFocusInWindow();
    }

    private void insert(String text) {
        boolean isOperator = text.length() == 1 && "+−×÷^%!".contains(text);
        if (justEvaluated && !isOperator) {
            input.setText(""); // start a new expression after "="
        }
        justEvaluated = false;
        int caret = input.getCaretPosition();
        String current = input.getText();
        input.setText(current.substring(0, caret) + text + current.substring(caret));
        input.setCaretPosition(caret + text.length());
    }

    private void backspace() {
        String text = input.getText();
        int caret = input.getCaretPosition();
        if (caret > 0) {
            input.setText(text.substring(0, caret - 1) + text.substring(caret));
            input.setCaretPosition(caret - 1);
        }
        justEvaluated = false;
    }

    private void memory(boolean add) {
        try {
            double value = input.getText().isBlank() ? calculator.getAns() : calculator.preview(input.getText());
            if (add) {
                calculator.memoryAdd(value);
            } else {
                calculator.memorySubtract(value);
            }
            updateStatus();
        } catch (CalculatorException ex) {
            showError(ex);
        }
    }

    private void evaluate() {
        String expression = input.getText();
        if (expression.isBlank()) {
            return;
        }
        try {
            double result = calculator.evaluate(expression);
            historyModel.add(0, calculator.getHistory().get(0));
            input.setText(Formatter.format(result));
            preview.setForeground(Color.GRAY);
            preview.setText(expression + " =");
            justEvaluated = true;
        } catch (CalculatorException ex) {
            showError(ex);
        }
    }

    private void updatePreview() {
        if (justEvaluated) {
            return;
        }
        String text = input.getText();
        preview.setForeground(Color.GRAY);
        try {
            preview.setText(text.isBlank() ? " " : "= " + Formatter.format(calculator.preview(text)));
        } catch (CalculatorException ex) {
            preview.setText(" "); // incomplete expressions are normal while typing
        }
    }

    private void showError(CalculatorException ex) {
        preview.setForeground(new Color(0xFF453A));
        preview.setText(ex.getMessage());
        if (ex.getPosition() >= 0 && ex.getPosition() <= input.getText().length()) {
            input.setCaretPosition(ex.getPosition());
        }
    }

    private void updateStatus() {
        String mode = calculator.getAngleMode() == AngleMode.DEGREES ? "DEG" : "RAD";
        status.setText(mode + (calculator.hasMemory()
                ? "   M = " + Formatter.format(calculator.memoryRecall()) : ""));
    }
}
