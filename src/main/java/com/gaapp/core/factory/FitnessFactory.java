package com.gaapp.core.factory;

import com.gaapp.core.factory.PopulationFactory.GeneType;
import com.gaapp.core.fitness.*;
import com.gaapp.core.problem.ProblemData;
import com.gaapp.core.problem.ProblemType;
import com.gaapp.core.util.ExpressionValidator;

public class FitnessFactory {

    public enum FitnessType {
        SUM,
        BINARY,
        ONES,
        TSP,
        KNAPSACK,
        CUSTOM
    }

    public static FitnessFunction create(
            FitnessType type,
            ProblemData problemData,
            PopulationFactory.GeneType geneType,
            FitnessConfig config,
            int geneLength
    ) {

        // =========================================================
        // VALIDATION LAYER
        // =========================================================

        validateCompatibility(type, problemData, geneType);

        // =========================================================
        // CREATION
        // =========================================================

        return switch (type) {

            case SUM -> new SumFitness();

            case BINARY -> new BinaryFitness();

            case ONES -> new SumFitness();

            case TSP -> {
                if (!(config instanceof TSPFitnessConfig tc)) {
                    throw new IllegalArgumentException("TSP config required");
                }
                yield new TSPFitness(tc.getDistanceMatrix());
            }

            case KNAPSACK -> {
                if (!(config instanceof KnapsackFitnessConfig kc)) {
                    throw new IllegalArgumentException("Knapsack config required");
                }
                yield new KnapsackFitness(
                        kc.getValues(),
                        kc.getWeights(),
                        kc.getCapacity()
                );
            }

            case CUSTOM -> {

                if (!(config instanceof CustomFitnessConfig cc)) {
                    throw new IllegalArgumentException("CustomFitnessConfig required");
                }

                String validatedExpr = ExpressionValidator.validate(
                        cc.getExpression(),
                        geneLength,
                        geneType
                );

                yield new CustomFitness(validatedExpr);
            }
        };
    }

    // =========================================================
    // RULE ENGINE (VERY IMPORTANT)
    // =========================================================

    private static void validateCompatibility(
            FitnessType type,
            ProblemData problemData,
            PopulationFactory.GeneType geneType
    ) {

        ProblemType problemType =
                (problemData == null)
                        ? ProblemType.GENERIC
                        : problemData.getType();

        switch (problemType) {

            case GENERIC -> {
                if (type == FitnessType.TSP || type == FitnessType.KNAPSACK) {
                    throw new IllegalArgumentException("Invalid fitness for GENERIC problem");
                }

                if ((type == FitnessType.ONES || type == FitnessType.BINARY) && geneType != PopulationFactory.GeneType.BINARY) {
                    throw new IllegalArgumentException("ONES and BINARY require binary genes");
                }
            }

            case TSP -> {
                if (type != FitnessType.TSP) {
                    throw new IllegalArgumentException("TSP problem requires TSP fitness");
                }
            }

            case KNAPSACK -> {
                if (type != FitnessType.KNAPSACK) {
                    throw new IllegalArgumentException("Knapsack requires Knapsack fitness");
                }
            }
        }
    }
}