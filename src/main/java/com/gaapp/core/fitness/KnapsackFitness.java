package com.gaapp.core.fitness;

import com.gaapp.core.model.Gene;
import com.gaapp.core.model.Individual;

import java.util.List;

public class KnapsackFitness implements FitnessFunction {

    private double[] weights;
    private double[] values;
    private double capacity;

    public KnapsackFitness(double[] weights, double[] values, double capacity) {
        this.weights = weights;
        this.values = values;
        this.capacity = capacity;
    }

    @Override
    public double evaluate(Individual individual) {
        double totalWeight = 0;
        double totalValue = 0;

        for (int i = 0; i < individual.getGenes().size(); i++) {
            int take = ((Number) individual.getGenes().get(i).getValue()).intValue();

            if (take == 1) {
                totalWeight += weights[i];
                totalValue += values[i];
            }
        }

        return (totalWeight > capacity) ? 0 : totalValue;
    }
}