package com.gaapp.core.fitness;

public class TSPFitnessConfig implements FitnessConfig {

    private final double[][] distanceMatrix;

    public TSPFitnessConfig(double[][] matrix) {
        this.distanceMatrix = matrix;
    }

    public double[][] getDistanceMatrix() {
        return distanceMatrix;
    }
}