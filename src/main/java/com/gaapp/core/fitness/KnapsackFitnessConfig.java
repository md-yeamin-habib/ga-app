package com.gaapp.core.fitness;

public class KnapsackFitnessConfig implements FitnessConfig {

    private final double[] values;
    private final double[] weights;
    private final double capacity;

    public KnapsackFitnessConfig(double[] values, double[] weights, double capacity) {
        this.values = values;
        this.weights = weights;
        this.capacity = capacity;
    }

    public double[] getValues() { return values; }
    public double[] getWeights() { return weights; }
    public double getCapacity() { return capacity; }
}
