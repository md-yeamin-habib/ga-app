package com.gaapp.core.fitness;

import com.gaapp.core.model.Gene;
import com.gaapp.core.model.Individual;

import java.util.List;

public class BinaryFitness implements FitnessFunction {

    @Override
    public double evaluate(Individual individual) {

        List<Gene> genes = individual.getGenes();

        double value = 0;
        int n = genes.size();

        for (int i = 0; i < n; i++) {
            Object val = genes.get(i).getValue();

            int bit = 0;
            if (val instanceof Integer) {
                bit = (Integer) val;
            }  else {
                throw new IllegalArgumentException("Unsupported gene value type: " + val.getClass());
            }
            value += bit * Math.pow(2, (n - i - 1));
        }

        return value;
    }
}