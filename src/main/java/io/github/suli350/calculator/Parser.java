package io.github.suli350.calculator;

import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * Recursive-descent parser and evaluator.
 *
 * <pre>
 * expression := term (('+' | '-') term)*
 * term       := unary (('*' | '/' | '%') unary)*
 * unary      := ('+' | '-') unary | power
 * power      := postfix ('^' unary)?          right associative: 2^3^2 = 2^9
 * postfix    := primary '!'*
 * primary    := NUMBER | constant | function '(' expression ')' | '(' expression ')'
 * </pre>
 *
 * Implicit multiplication is supported between a value and a following
 * '(' , constant or function: 2(3+4), 2pi, 3sin(30).
 */
final class Parser {

    private final List<Token> tokens;
    private final AngleMode mode;
    private final Map<String, Double> variables;
    private int pos;

    Parser(List<Token> tokens, AngleMode mode, Map<String, Double> variables) {
        this.tokens = tokens;
        this.mode = mode;
        this.variables = variables;
    }

    double parse() {
        if (peek().type == Token.Type.END) {
            throw new CalculatorException("Empty expression", 0);
        }
        double result = expression();
        if (peek().type != Token.Type.END) {
            Token t = peek();
            throw new CalculatorException("Unexpected '" + t.text + "'", t.position);
        }
        return result;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token next() {
        return tokens.get(pos++);
    }

    private double expression() {
        double value = term();
        while (peek().is("+") || peek().is("-")) {
            if (next().text.equals("+")) {
                value += term();
            } else {
                value -= term();
            }
        }
        return value;
    }

    private double term() {
        double value = unary();
        while (true) {
            Token t = peek();
            if (t.is("*")) {
                next();
                value *= unary();
            } else if (t.is("/")) {
                next();
                double divisor = unary();
                if (divisor == 0) {
                    throw new CalculatorException("Division by zero", t.position);
                }
                value /= divisor;
            } else if (t.is("%")) {
                next();
                double divisor = unary();
                if (divisor == 0) {
                    throw new CalculatorException("Modulo by zero", t.position);
                }
                value %= divisor;
            } else if (startsImplicitFactor(t)) {
                value *= unary();
            } else {
                return value;
            }
        }
    }

    private static boolean startsImplicitFactor(Token t) {
        return t.type == Token.Type.LEFT_PAREN || t.type == Token.Type.IDENTIFIER;
    }

    private double unary() {
        if (peek().is("-")) {
            next();
            return -unary();
        }
        if (peek().is("+")) {
            next();
            return unary();
        }
        return power();
    }

    private double power() {
        double base = postfix();
        if (peek().is("^")) {
            Token op = next();
            double exponent = unary();
            double result = Math.pow(base, exponent);
            if (Double.isNaN(result)) {
                throw new CalculatorException("Undefined power", op.position);
            }
            return result;
        }
        return base;
    }

    private double postfix() {
        double value = primary();
        while (peek().is("!")) {
            Token op = next();
            value = factorial(value, op.position);
        }
        return value;
    }

    private double primary() {
        Token t = next();
        switch (t.type) {
            case NUMBER:
                return t.value;
            case LEFT_PAREN: {
                double value = expression();
                expect(Token.Type.RIGHT_PAREN, "Missing ')'");
                return value;
            }
            case IDENTIFIER:
                return identifier(t);
            case END:
                throw new CalculatorException("Unexpected end of expression", t.position);
            default:
                throw new CalculatorException("Unexpected '" + t.text + "'", t.position);
        }
    }

    private double identifier(Token t) {
        String name = t.text;
        if (name.equals("pi") || name.equals("π")) {
            return Math.PI;
        }
        if (name.equals("e")) {
            return Math.E;
        }
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        DoubleUnaryOperator fn = function(name, t.position);
        expect(Token.Type.LEFT_PAREN, "Expected '(' after " + name);
        double arg = expression();
        expect(Token.Type.RIGHT_PAREN, "Missing ')'");
        double result = fn.applyAsDouble(arg);
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            throw new CalculatorException(name + "(" + Formatter.format(arg) + ") is undefined", t.position);
        }
        return result;
    }

    private DoubleUnaryOperator function(String name, int position) {
        switch (name) {
            case "sin": return x -> Math.sin(mode.toRadians(x));
            case "cos": return x -> Math.cos(mode.toRadians(x));
            case "tan": return this::tan;
            case "asin": return x -> mode.fromRadians(Math.asin(x));
            case "acos": return x -> mode.fromRadians(Math.acos(x));
            case "atan": return x -> mode.fromRadians(Math.atan(x));
            case "sqrt":
            case "√": return Math::sqrt;
            case "cbrt": return Math::cbrt;
            case "ln": return x -> x > 0 ? Math.log(x) : Double.NaN;
            case "log": return x -> x > 0 ? Math.log10(x) : Double.NaN;
            case "exp": return Math::exp;
            case "abs": return Math::abs;
            default:
                throw new CalculatorException("Unknown function '" + name + "'", position);
        }
    }

    private double tan(double x) {
        double radians = mode.toRadians(x);
        if (Math.abs(Math.cos(radians)) < 1e-12) {
            return Double.NaN; // tan(90°) and friends
        }
        return Math.tan(radians);
    }

    private void expect(Token.Type type, String message) {
        Token t = next();
        if (t.type != type) {
            throw new CalculatorException(message, t.position);
        }
    }

    static double factorial(double n, int position) {
        if (n < 0 || n != Math.floor(n)) {
            throw new CalculatorException("Factorial needs a non-negative integer", position);
        }
        if (n > 170) {
            throw new CalculatorException("Factorial too large", position);
        }
        double result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}
