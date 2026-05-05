package com.gaapp.core.util;

import java.util.*;

public class ExpressionEvaluator {

    public static double evaluate(String expr, double[] vars) {
        try {
            List<String> tokens = tokenize(expr);
            List<String> rpn = toRPN(tokens);
            return evalRPN(rpn, vars);
        } catch (ArithmeticException e) {
            System.out.println("Division by zero not allowed!");
            return -1e18; // worst fitness
        }
    }

    // --------------------------
    // TOKENIZER
    // --------------------------
    private static List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (char c : expr.toCharArray()) {

            if (Character.isWhitespace(c)) continue;

            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else {
                if (sb.length() > 0) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                }
                tokens.add(String.valueOf(c));
            }
        }

        if (sb.length() > 0) tokens.add(sb.toString());
        return tokens;
    }

    // --------------------------
    // INFIX → RPN
    // --------------------------
    private static List<String> toRPN(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> ops = new Stack<>();

        for (String t : tokens) {

            if (isNumberOrVariable(t)) {
                output.add(t);
            }
            else if (isOperator(t)) {

                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(t)) {
                    output.add(ops.pop());
                }
                ops.push(t);
            }
            else if (t.equals("(")) {
                ops.push(t);
            }
            else if (t.equals(")")) {
                while (!ops.peek().equals("(")) {
                    output.add(ops.pop());
                }
                ops.pop();
            }
        }

        while (!ops.isEmpty()) {
            output.add(ops.pop());
        }

        return output;
    }

    // --------------------------
    // RPN EVALUATION
    // --------------------------
    private static double evalRPN(List<String> rpn, double[] vars) {

        Stack<Double> stack = new Stack<>();

        for (String t : rpn) {

            if (isNumber(t)) {
                stack.push(Double.parseDouble(t));
            }

            else if (isVariable(t)) {
                int idx = Integer.parseInt(t.substring(1));
                stack.push(vars[idx]);
            }

            else if (isOperator(t)) {

                double b = stack.pop();
                double a = stack.pop();

                switch (t) {

                    case "+" -> stack.push(a + b);
                    case "-" -> stack.push(a - b);
                    case "*" -> stack.push(a * b);

                    case "/" -> {
                        if (b == 0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        stack.push(a / b);
                    }

                    case "%" -> {
                        if (b == 0) {
                            throw new ArithmeticException("Modulo by zero");
                        }
                        stack.push(a % b);
                    }

                    case "^" -> stack.push(Math.pow(a, b));
                }
            }
        }

        return stack.pop();
    }

    // --------------------------
    // HELPERS
    // --------------------------
    private static boolean isOperator(String t) {
        return "+-*/%^".contains(t);
    }

    private static boolean isVariable(String t) {
        return t.startsWith("x");
    }

    private static boolean isNumber(String t) {
        return t.matches("\\d+(\\.\\d+)?");
    }

    private static boolean isNumberOrVariable(String t) {
        return isNumber(t) || isVariable(t);
    }

    private static int precedence(String op) {
        return switch (op) {
            case "+", "-" -> 1;
            case "*", "/", "%" -> 2;
            case "^" -> 3;
            default -> -1;
        };
    }
}