package io.github.suli350.calculator;

import java.util.ArrayList;
import java.util.List;

/** Splits an expression like "2*sin(30)+3!" into tokens. */
final class Lexer {

    private Lexer() {
    }

    static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if (Character.isDigit(c) || c == '.') {
                int start = i;
                while (i < input.length() && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                    i++;
                }
                // optional exponent: 1.5e-3
                if (i < input.length() && (input.charAt(i) == 'e' || input.charAt(i) == 'E')
                        && i + 1 < input.length()
                        && (Character.isDigit(input.charAt(i + 1))
                            || ((input.charAt(i + 1) == '-' || input.charAt(i + 1) == '+')
                                && i + 2 < input.length() && Character.isDigit(input.charAt(i + 2))))) {
                    i += 2;
                    while (i < input.length() && Character.isDigit(input.charAt(i))) {
                        i++;
                    }
                }
                String text = input.substring(start, i);
                try {
                    tokens.add(new Token(Token.Type.NUMBER, text, Double.parseDouble(text), start));
                } catch (NumberFormatException e) {
                    throw new CalculatorException("Invalid number '" + text + "'", start);
                }
            } else if (Character.isLetter(c) || c == 'π' || c == '√') {
                int start = i;
                if (c == 'π' || c == '√') {
                    i++;
                } else {
                    while (i < input.length() && Character.isLetterOrDigit(input.charAt(i))) {
                        i++;
                    }
                }
                tokens.add(new Token(Token.Type.IDENTIFIER, input.substring(start, i).toLowerCase(), 0, start));
            } else if (c == '(') {
                tokens.add(new Token(Token.Type.LEFT_PAREN, "(", 0, i++));
            } else if (c == ')') {
                tokens.add(new Token(Token.Type.RIGHT_PAREN, ")", 0, i++));
            } else if ("+-*/^%!×÷−".indexOf(c) >= 0) {
                String op = c == '×' ? "*" : c == '÷' ? "/" : c == '−' ? "-" : String.valueOf(c);
                tokens.add(new Token(Token.Type.OPERATOR, op, 0, i++));
            } else {
                throw new CalculatorException("Unexpected character '" + c + "'", i);
            }
        }
        tokens.add(new Token(Token.Type.END, "", 0, input.length()));
        return tokens;
    }
}
