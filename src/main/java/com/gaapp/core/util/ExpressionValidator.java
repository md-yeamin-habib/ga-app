package com.gaapp.core.util;

import com.gaapp.core.factory.PopulationFactory.GeneType;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionValidator {

    private static final Pattern ALLOWED_PATTERN =
            Pattern.compile("[0-9x+\\-*/%^().\\s]+");

    private static final Pattern VARIABLE_PATTERN =
            Pattern.compile("x(\\d+)");

    // --------------------------
    // MAIN VALIDATION
    // --------------------------
    public static String validate(String expr, int geneLength, GeneType geneType) {

        if (expr == null || expr.isBlank()) {
            throw new IllegalArgumentException("Expression cannot be empty");
        }

        // 1. character safety
        Pattern pattern = ALLOWED_PATTERN;

        if (geneType == GeneType.BINARY) {
            pattern = Pattern.compile("[01x+\\-*/%^().\\s]+");
        }

        if (!pattern.matcher(expr).matches()) {
            throw new IllegalArgumentException("Expression contains invalid characters");
        }

        // 2. bracket validation
        validateBrackets(expr);

        // 3. variable index validation
        validateVariables(expr, geneLength);

        // 4. division/modulo by zero check (static scan)
        validateDivisionSafety(expr);

        return expr;
    }

    // --------------------------
    // BRACKET CHECK
    // --------------------------
    private static void validateBrackets(String expr) {
        Stack<Character> stack = new Stack<>();

        for (char c : expr.toCharArray()) {
            if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Unbalanced closing bracket ')'");
                }
                stack.pop();
            }
        }

        if (!stack.isEmpty()) {
            throw new IllegalArgumentException("Unbalanced opening bracket '('");
        }
    }

    // --------------------------
    // VARIABLE CHECK
    // --------------------------
    private static void validateVariables(String expr, int geneLength) {
        Matcher matcher = VARIABLE_PATTERN.matcher(expr);

        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1));

            if (index < 0 || index >= geneLength) {
                throw new IllegalArgumentException(
                        "Invalid variable index x" + index +
                        " for geneLength " + geneLength
                );
            }
        }
    }

    // --------------------------
    // DIVISION / MOD SAFETY
    // --------------------------
    private static void validateDivisionSafety(String expr) {

        // remove spaces for easier scanning
        String cleaned = expr.replaceAll("\\s+", "");

        // detect /0 or %0 patterns
        // handles:  x / 0,  (3+2)/0, x1%0
        Pattern unsafePattern = Pattern.compile("[/%]\\s*0+(?!\\d)");

        Matcher m = unsafePattern.matcher(cleaned);

        if (m.find()) {
            throw new IllegalArgumentException("Division or modulus by zero detected");
        }
    }
}