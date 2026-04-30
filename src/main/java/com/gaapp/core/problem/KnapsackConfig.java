package com.gaapp.core.problem;

public class KnapsackConfig implements ProblemConfig {

    private final double minValue;
    private final double maxValue;
    private final double minWeight;
    private final double maxWeight;
    private final double capacity;

    public KnapsackConfig(
            double minValue,
            double maxValue,
            double minWeight,
            double maxWeight,
            double capacity
    ) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.capacity = capacity;
    }

    public double getMinValue() { return minValue; }
    public double getMaxValue() { return maxValue; }
    public double getMinWeight() { return minWeight; }
    public double getMaxWeight() { return maxWeight; }
    public double getCapacity() { return capacity; }
}