package io.github.suli350.calculator;

/** Thrown for any invalid expression or undefined result (e.g. division by zero). */
public class CalculatorException extends RuntimeException {

    private final int position;

    public CalculatorException(String message) {
        this(message, -1);
    }

    public CalculatorException(String message, int position) {
        super(message);
        this.position = position;
    }

    /** Character index in the expression where the problem was found, or -1. */
    public int getPosition() {
        return position;
    }
}
