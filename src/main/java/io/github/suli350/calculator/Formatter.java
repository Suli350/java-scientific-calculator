package io.github.suli350.calculator;

import java.math.BigDecimal;
import java.math.MathContext;

/** Turns doubles into display strings: 0.1+0.2 shows as 0.3, huge values in E notation. */
public final class Formatter {

    private static final int SIGNIFICANT_DIGITS = 12;

    private Formatter() {
    }

    public static String format(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "∞" : "-∞";
        }
        double abs = Math.abs(value);
        if (abs != 0 && (abs >= 1e15 || abs < 1e-9)) {
            BigDecimal bd = new BigDecimal(value).round(new MathContext(10));
            String s = String.format("%.9E", bd.doubleValue());
            // 1.230000000E+20 -> 1.23E20
            String[] parts = s.split("E");
            String mantissa = parts[0].replaceAll("0+$", "").replaceAll("\\.$", "");
            int exponent = Integer.parseInt(parts[1]);
            return mantissa + "E" + exponent;
        }
        BigDecimal bd = new BigDecimal(value).round(new MathContext(SIGNIFICANT_DIGITS)).stripTrailingZeros();
        String s = bd.toPlainString();
        return s.equals("-0") ? "0" : s;
    }
}
