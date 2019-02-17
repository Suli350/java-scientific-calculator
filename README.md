# Scientific Calculator (Java / Swing)

A fully working desktop scientific calculator written in plain Java 11 and Swing,
with a hand-written **recursive-descent expression parser**.

## Features

- Type expressions or use the keypad: `2(3+4)^2 / sin(30) + 5!`
- Operators `+ - × ÷ % ^ !` with correct precedence (`2^3^2 = 512`, `-2^2 = -4`)
- Functions: `sin cos tan asin acos atan sqrt √ cbrt ln log exp abs`
- Constants `π`, `e`, and `ans` (previous result)
- Implicit multiplication: `2π`, `3sin(30)`, `2(1+1)`
- Degree / radian mode
- Memory: `MC MR M+ M-`
- Live result preview while typing
- History panel (double-click to reuse an expression)
- Clear error messages, with the cursor moved to the problem
- Command-line mode: `java -jar target/scientific-calculator-1.0.0.jar "2*sin(30)+3!"`

## Project structure

```
src/main/java/io/github/suli350/calculator/
├── Main.java               entry point (GUI or CLI)
├── CalculatorFrame.java    Swing UI
├── Calculator.java         engine: ans, memory, history, angle mode
├── Lexer.java / Token.java tokenizer
├── Parser.java             recursive-descent parser + evaluator
├── Formatter.java          number formatting (0.1+0.2 -> 0.3)
└── AngleMode.java, CalculatorException.java
```

## Build and run

Requires JDK 11+ and Maven.

```bash
mvn test                 # run the unit tests
mvn package              # build target/scientific-calculator-1.0.0.jar
java -jar target/scientific-calculator-1.0.0.jar
```

## Grammar

```
expression := term (('+' | '-') term)*
term       := unary (('*' | '/' | '%') unary)*
unary      := ('+' | '-') unary | power
power      := postfix ('^' unary)?
postfix    := primary '!'*
primary    := NUMBER | constant | function '(' expression ')' | '(' expression ')'
```

## Ideas for extending it

- Variables (`x = 5`) and user-defined functions
- A graphing panel that plots `f(x)`
- Programmer mode (hex/binary, bitwise operators)
