package com.gaapp.core.fitness;

public class CustomFitnessConfig implements FitnessConfig {

    private final String expression;

    public CustomFitnessConfig(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }
}
