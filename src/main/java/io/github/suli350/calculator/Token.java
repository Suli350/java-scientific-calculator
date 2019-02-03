package io.github.suli350.calculator;

final class Token {

    enum Type { NUMBER, IDENTIFIER, OPERATOR, LEFT_PAREN, RIGHT_PAREN, END }

    final Type type;
    final String text;
    final double value;
    final int position;

    Token(Type type, String text, double value, int position) {
        this.type = type;
        this.text = text;
        this.value = value;
        this.position = position;
    }

    boolean is(String op) {
        return type == Type.OPERATOR && text.equals(op);
    }

    @Override
    public String toString() {
        return type + "(" + text + ")";
    }
}
