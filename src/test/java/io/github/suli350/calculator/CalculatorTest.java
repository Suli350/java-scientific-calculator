package io.github.suli350.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalculatorTest {

    private static final double EPS = 1e-9;
    private Calculator calc;

    @BeforeEach
    void setUp() {
        calc = new Calculator();
    }

    @Test
    void basicArithmeticAndPrecedence() {
        assertEquals(14, calc.evaluate("2+3*4"), EPS);
        assertEquals(20, calc.evaluate("(2+3)*4"), EPS);
        assertEquals(2.5, calc.evaluate("10/4"), EPS);
        assertEquals(1, calc.evaluate("10 % 3"), EPS);
        assertEquals(-1, calc.evaluate("2-3"), EPS);
    }

    @Test
    void powerIsRightAssociativeAndBindsTighterThanUnaryMinus() {
        assertEquals(512, calc.evaluate("2^3^2"), EPS);
        assertEquals(-4, calc.evaluate("-2^2"), EPS);
        assertEquals(0.5, calc.evaluate("2^-1"), EPS);
    }

    @Test
    void implicitMultiplication() {
        assertEquals(14, calc.evaluate("2(3+4)"), EPS);
        assertEquals(2 * Math.PI, calc.evaluate("2pi"), EPS);
        assertEquals(1.5, calc.evaluate("3sin(30)"), EPS);
    }

    @Test
    void trigonometryInDegreesAndRadians() {
        assertEquals(0.5, calc.evaluate("sin(30)"), EPS);
        assertEquals(60, calc.evaluate("acos(0.5)"), EPS);
        calc.setAngleMode(AngleMode.RADIANS);
        assertEquals(1, calc.evaluate("sin(pi/2)"), EPS);
        assertEquals(Math.PI / 4, calc.evaluate("atan(1)"), EPS);
    }

    @Test
    void functionsAndConstants() {
        assertEquals(3, calc.evaluate("sqrt(9)"), EPS);
        assertEquals(3, calc.evaluate("√(9)"), EPS);
        assertEquals(2, calc.evaluate("log(100)"), EPS);
        assertEquals(1, calc.evaluate("ln(e)"), EPS);
        assertEquals(120, calc.evaluate("5!"), EPS);
        assertEquals(720, calc.evaluate("3!!"), EPS);
        assertEquals(5, calc.evaluate("abs(-5)"), EPS);
        assertEquals(1500, calc.evaluate("1.5e3"), EPS);
    }

    @Test
    void unicodeOperatorsFromTheKeypad() {
        assertEquals(6, calc.evaluate("2×3"), EPS);
        assertEquals(2, calc.evaluate("6÷3"), EPS);
        assertEquals(1, calc.evaluate("3−2"), EPS);
    }

    @Test
    void ansHoldsThePreviousResult() {
        calc.evaluate("6*7");
        assertEquals(84, calc.evaluate("ans*2"), EPS);
        assertEquals(84, calc.getAns(), EPS);
    }

    @Test
    void errorsAreReportedWithPosition() {
        CalculatorException ex = assertThrows(CalculatorException.class, () -> calc.evaluate("5/0"));
        assertEquals("Division by zero", ex.getMessage());
        assertThrows(CalculatorException.class, () -> calc.evaluate("(2+3"));
        assertThrows(CalculatorException.class, () -> calc.evaluate("2+"));
        assertThrows(CalculatorException.class, () -> calc.evaluate("foo(2)"));
        assertThrows(CalculatorException.class, () -> calc.evaluate("sqrt(-1)"));
        assertThrows(CalculatorException.class, () -> calc.evaluate("tan(90)"));
        assertThrows(CalculatorException.class, () -> calc.evaluate("2.5!"));
        assertThrows(CalculatorException.class, () -> calc.evaluate("2 $ 3"));
        assertThrows(CalculatorException.class, () -> calc.evaluate(""));
        assertEquals(4, assertThrows(CalculatorException.class, () -> calc.evaluate("1 + )")).getPosition());
    }

    @Test
    void memoryOperations() {
        assertFalse(calc.hasMemory());
        calc.memoryAdd(10);
        calc.memorySubtract(3);
        assertTrue(calc.hasMemory());
        assertEquals(7, calc.memoryRecall(), EPS);
        calc.memoryClear();
        assertEquals(0, calc.memoryRecall(), EPS);
    }

    @Test
    void historyIsNewestFirstAndPreviewDoesNotRecord() {
        calc.evaluate("1+1");
        calc.evaluate("2*3");
        calc.preview("9*9");
        assertEquals(2, calc.getHistory().size());
        assertEquals("2*3 = 6", calc.getHistory().get(0));
    }
}
