package com.gaapp.core.factory;

import com.gaapp.core.replacement.*;

public class ReplacementFactory {

    public enum ReplacementType {
        FULL,
        ELITISM
    }

    // -------------------------
    // CONFIG WRAPPER
    // -------------------------
    public static class ReplacementConfig {

        private final ReplacementStrategy strategy;
        private final Integer eliteCount;

        public ReplacementConfig(ReplacementStrategy strategy) {
            this(strategy, null);
        }

        public ReplacementConfig(ReplacementStrategy strategy, Integer eliteCount) {
            this.strategy = strategy;
            this.eliteCount = eliteCount;
        }

        public ReplacementStrategy getStrategy() {
            return strategy;
        }

        public Integer getEliteCount() {
            return eliteCount;
        }
    }

    // -------------------------
    // CREATE FACTORY
    // -------------------------
    public static ReplacementConfig create(ReplacementType type, int... args) {

        return switch (type) {

            case FULL -> new ReplacementConfig(
                    new FullReplacement()
            );

            case ELITISM -> {

                int eliteCount = 2;

                if (args.length > 0) {
                    eliteCount = args[0];
                }

                yield new ReplacementConfig(
                        new ElitismReplacement(eliteCount),
                        eliteCount
                );
            }
        };
    }
}