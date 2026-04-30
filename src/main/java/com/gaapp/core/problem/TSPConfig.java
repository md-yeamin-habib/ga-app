package com.gaapp.core.problem;

public class TSPConfig implements ProblemConfig {

    private final double minCoord;
    private final double maxCoord;

    public TSPConfig(double minCoord, double maxCoord) {
        this.minCoord = minCoord;
        this.maxCoord = maxCoord;
    }

    public double getMinCoord() { return minCoord; }
    public double getMaxCoord() { return maxCoord; }
}