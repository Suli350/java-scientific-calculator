package io.github.suli350.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FormatterTest {

    @Test
    void hidesFloatingPointNoise() {
        assertEquals("0.3", Formatter.format(0.1 + 0.2));
        assertEquals("0.5", Formatter.format(Math.sin(Math.toRadians(30))));
    }

    @Test
    void integersHaveNoDecimalPoint() {
        assertEquals("42", Formatter.format(42.0));
        assertEquals("0", Formatter.format(-0.0));
        assertEquals("-7", Formatter.format(-7));
    }

    @Test
    void veryLargeAndSmallUseENotation() {
        assertEquals("1.23E20", Formatter.format(1.23e20));
        assertEquals("5E-12", Formatter.format(5e-12));
    }
}
