package com.gaapp.core.fitness;

import com.gaapp.core.model.Gene;
import com.gaapp.core.model.Individual;
import com.gaapp.core.util.ExpressionEvaluator;

import java.util.List;

public class CustomFitness implements FitnessFunction {

    private final String expression;

    public CustomFitness(String validatedExpression) {
        this.expression = validatedExpression;
    }

    @Override
    public double evaluate(Individual individual) {

        List<Gene> genes = individual.getGenes();

        double[] vars = new double[genes.size()];

        for (int i = 0; i < genes.size(); i++) {
            Object val = genes.get(i).getValue();

            if (val instanceof Number n) {
                vars[i] = n.doubleValue();
            } else {
                vars[i] = 0;
            }
        }

        return ExpressionEvaluator.evaluate(expression, vars);
    }
}