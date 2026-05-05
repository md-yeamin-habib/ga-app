package com.gaapp.core.fitness;

import com.gaapp.core.model.Gene;
import com.gaapp.core.model.Individual;

import java.util.List;

public class TSPFitness implements FitnessFunction {

    private double[][] matrix;

    public TSPFitness(double[][] matrix) {
        this.matrix = matrix;
    }

    @Override
    public double evaluate(Individual individual) {
        double total = 0;
        List<Gene> genes = individual.getGenes();

        for (int i = 0; i < genes.size() - 1; i++) {
            int from = (int) genes.get(i).getValue();
            int to = (int) genes.get(i + 1).getValue();
            total += matrix[from][to];
        }

        // return to start
        int last = (int) genes.get(genes.size() - 1).getValue();
        int first = (int) genes.get(0).getValue();
        total += matrix[last][first];

        return 1.0 / total;
    }
}