package com.gaapp.core.problem;

import com.gaapp.core.problem.ProblemData;
import com.gaapp.core.problem.ProblemType;

public class KnapsackProblem implements ProblemData {

    private final double[] values;
    private final double[] weights;
    private final double capacity;

    public KnapsackProblem(double[] values, double[] weights, double capacity) {

        if (values.length != weights.length) {
            throw new IllegalArgumentException("Values and weights must have same length");
        }

        if (values == null || weights == null || values.length == 0 || weights.length == 0) {
            throw new IllegalArgumentException("Values and weights cannot be null or empty");
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be > 0");
        }

        this.values = values.clone();
        this.weights = weights.clone();
        this.capacity = capacity;
    }

    @Override
    public ProblemType getType() {
        return ProblemType.KNAPSACK;
    }

    public int size() {
        return values.length;
    }

    public double getValue(int i) {
        return values[i];
    }

    public double getWeight(int i) {
        return weights[i];
    }

    public double getCapacity() {
        return capacity;
    }

    public double[] getValues() {
        return values.clone();
    }

    public double[] getWeights() {
        return weights.clone();
    }

    @Override
    public void printDebug() {

        System.out.println("\n=== KNAPSACK PROBLEM ===");
        System.out.println("Items: " + values.length);
        System.out.println("Capacity: " + capacity);

        System.out.println("\nIndex | Value | Weight");
        for (int i = 0; i < values.length; i++) {
            System.out.printf("%5d | %6.2f | %6.2f%n", i, values[i], weights[i]);
        }
    }
}