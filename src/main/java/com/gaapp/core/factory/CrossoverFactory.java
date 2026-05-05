package com.gaapp.core.factory;

import com.gaapp.core.crossover.*;
import com.gaapp.core.factory.FitnessFactory.FitnessType;

public class CrossoverFactory {

    public enum CrossoverType {
        ONE_POINT,
        TWO_POINT,
        ORDER
    }

    public static class CrossoverConfig {
        private final CrossoverStrategy strategy;
        private final int[] defaultIndices;

        public CrossoverConfig(CrossoverStrategy strategy, int[] defaultIndices) {
            this.strategy = strategy;
            this.defaultIndices = defaultIndices;
        }

        public CrossoverStrategy getStrategy() {
            return strategy;
        }

        public int[] getDefaultIndices() {
            return defaultIndices;
        }
    }

    // -------------------------
    // FACTORY CREATION
    // -------------------------
    public static CrossoverConfig create(CrossoverType type, int... defaultIndices) {

        return switch (type) {

            case ONE_POINT -> new CrossoverConfig(
                    new OnePointCrossover(),
                    normalize(defaultIndices)
            );

            case TWO_POINT -> new CrossoverConfig(
                    new TwoPointCrossover(),
                    normalize(defaultIndices)
            );

            case ORDER -> new CrossoverConfig(
                    new OrderCrossover(),
                    normalize(defaultIndices)
            );
        };
    }

    // -------------------------
    // VALIDATION RULES
    // -------------------------
    public static void validate(FitnessType fitnessType, CrossoverType crossoverType) {

        switch (fitnessType) {

            case TSP -> {
                if (crossoverType != CrossoverType.ORDER &&
                    crossoverType != CrossoverType.TWO_POINT) {

                    throw new IllegalArgumentException(
                            "TSP requires ORDER or TWO_POINT crossover"
                    );
                }
            }

            case KNAPSACK, BINARY -> {
                if (crossoverType == CrossoverType.ORDER) {

                    throw new IllegalArgumentException(
                            "ORDER crossover not allowed for BINARY/KNAPSACK"
                    );
                }
            }

            case SUM, CUSTOM, ONES -> {
                // all allowed
            }
        }
    }

    // -------------------------
    // PARAM NORMALIZER
    // -------------------------
    private static int[] normalize(int... indices) {
        return (indices == null) ? new int[0] : indices;
    }
}