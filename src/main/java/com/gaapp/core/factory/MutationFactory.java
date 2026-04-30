package com.gaapp.core.factory;

import com.gaapp.core.factory.FitnessFactory;
import com.gaapp.core.mutation.*;

public class MutationFactory {

    public enum MutationType {
        BIT_FLIP,
        RANDOM_RESET,
        SWAP,
        INVERSION,
        SCRAMBLE
    }

    // -------------------------
    // CONFIG WRAPPER
    // -------------------------
    public static class MutationConfig {

        private final MutationStrategy strategy;
        private final Integer minValue;
        private final Integer maxValue;

        public MutationConfig(MutationStrategy strategy) {
            this(strategy, null, null);
        }

        public MutationConfig(MutationStrategy strategy, Integer minValue, Integer maxValue) {
            this.strategy = strategy;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public MutationStrategy getStrategy() {
            return strategy;
        }

        public Integer getMinValue() {
            return minValue;
        }

        public Integer getMaxValue() {
            return maxValue;
        }
    }

    // -------------------------
    // CREATE FACTORY
    // -------------------------
    public static MutationConfig create(MutationType type, int... args) {

        return switch (type) {

            case BIT_FLIP -> new MutationConfig(
                    new BitFlipMutation()
            );

            case RANDOM_RESET -> {

                int min = 0;
                int max = 9;

                if (args.length > 0) min = args[0];
                if (args.length > 1) max = args[1];

                yield new MutationConfig(
                        new RandomResetMutation(min, max),
                        min,
                        max
                );
            }

            case SWAP -> new MutationConfig(
                    new SwapMutation()
            );

            case INVERSION -> new MutationConfig(
                    new InversionMutation()
            );

            case SCRAMBLE -> new MutationConfig(
                    new ScrambleMutation()
            );
        };
    }

    // -------------------------
    // VALIDATION LAYER
    // -------------------------
    public static void validate(FitnessFactory.FitnessType fitnessType, MutationType mutationType) {

        switch (fitnessType) {

            // ---------------- BINARY / KNAPSACK ----------------
            case BINARY, ONES, KNAPSACK -> {

                if (mutationType == MutationType.RANDOM_RESET) {

                    throw new IllegalArgumentException(
                            "Permutation mutations not allowed for Binary/Knapsack"
                    );
                }
            }

            // ---------------- TSP ----------------
            case TSP -> {

                if (mutationType == MutationType.BIT_FLIP ||
                    mutationType == MutationType.RANDOM_RESET) {

                    throw new IllegalArgumentException(
                            "BitFlip/RandomReset not allowed for TSP"
                    );
                }
            }

            // ---------------- SUM / CUSTOM ----------------
            case SUM, CUSTOM -> {
                
                if (mutationType == MutationType.BIT_FLIP) {

                    throw new IllegalArgumentException(
                            "BitFlip not allowed for Sum/Custom"
                    );
                }
            }
        }
    }
}