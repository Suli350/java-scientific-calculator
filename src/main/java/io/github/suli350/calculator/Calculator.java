package io.github.suli350.calculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The calculator "engine": evaluation, angle mode, ans, memory and history. No Swing here. */
public class Calculator {

    public static final int MAX_HISTORY = 100;

    private AngleMode angleMode = AngleMode.DEGREES;
    private double ans;
    private double memory;
    private final List<String> history = new ArrayList<>();

    public double evaluate(String expression) {
        Map<String, Double> variables = new HashMap<>();
        variables.put("ans", ans);
        double result = new Parser(Lexer.tokenize(expression), angleMode, variables).parse();
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            throw new CalculatorException("Result is undefined");
        }
        if (result == 0) {
            result = 0; // normalise -0.0
        }
        ans = result;
        history.add(0, expression.trim() + " = " + Formatter.format(result));
        if (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
        }
        return result;
    }

    /** Evaluate without touching ans or history (used for the live preview). */
    public double preview(String expression) {
        Map<String, Double> variables = new HashMap<>();
        variables.put("ans", ans);
        return new Parser(Lexer.tokenize(expression), angleMode, variables).parse();
    }

    public AngleMode getAngleMode() {
        return angleMode;
    }

    public void setAngleMode(AngleMode angleMode) {
        this.angleMode = angleMode;
    }

    public AngleMode toggleAngleMode() {
        angleMode = angleMode == AngleMode.DEGREES ? AngleMode.RADIANS : AngleMode.DEGREES;
        return angleMode;
    }

    public double getAns() {
        return ans;
    }

    // ------------------------------------------------------------------ memory
    public void memoryClear() {
        memory = 0;
    }

    public double memoryRecall() {
        return memory;
    }

    public void memoryAdd(double value) {
        memory += value;
    }

    public void memorySubtract(double value) {
        memory -= value;
    }

    public boolean hasMemory() {
        return memory != 0;
    }

    // ------------------------------------------------------------------ history
    /** Newest first, formatted as "expression = result". */
    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void clearHistory() {
        history.clear();
    }
}
