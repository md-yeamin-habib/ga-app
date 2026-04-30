package com.gaapp.core.fitness;

import com.gaapp.core.model.Gene;
import com.gaapp.core.model.Individual;

public class SumFitness implements FitnessFunction {

    @Override
    public double evaluate(Individual individual) {
        double sum = 0;

        for (Gene g : individual.getGenes()) {
            Object val = g.getValue();

            if (val instanceof Number) {
                sum += ((Number) val).doubleValue();
            } else if (val instanceof Boolean) {
                sum += (Boolean) val ? 1 : 0;
            }
        }

        return sum;
    }
}