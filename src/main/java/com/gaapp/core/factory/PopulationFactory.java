package com.gaapp.core.factory;

import com.gaapp.core.model.*;
import com.gaapp.core.problem.*;
import com.gaapp.core.util.DistanceUtil;
import com.gaapp.core.util.RandomUtil;

import com.gaapp.core.factory.PopulationBundle;
import com.gaapp.core.problem.KnapsackConfig;
import com.gaapp.core.problem.Point;
import com.gaapp.core.problem.ProblemConfig;
import com.gaapp.core.problem.ProblemType;
import com.gaapp.core.problem.TSPConfig;

import java.util.ArrayList;
import java.util.List;

public class PopulationFactory {

    public enum GeneType {
        BINARY,
        INTEGER,
        REAL
    }

    // =========================================================
    // GENERIC POPULATION
    // =========================================================

    public static Population createRandomPopulation(
            int populationSize,
            int geneLength,
            int minGeneValue,
            int maxGeneValue,
            GeneType geneType
    ) {

        if (populationSize <= 0 || geneLength <= 0) {
            throw new IllegalArgumentException("Population size and gene length must be > 0");
        }

        List<Individual> individuals = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {

            List<Gene> genes = new ArrayList<>();

            for (int j = 0; j < geneLength; j++) {
                genes.add(generateGene(geneType, minGeneValue, maxGeneValue));
            }

            individuals.add(new Individual(genes, "X" + (individuals.size() + 1)));
        }

        return new Population(individuals);
    }

    private static Gene generateGene(GeneType type, int min, int max) {
        return switch (type) {
            case BINARY -> new BinaryGene(RandomUtil.nextInt(2));
            case INTEGER -> new IntegerGene(RandomUtil.nextInt(min, max));
            case REAL -> new RealGene(RandomUtil.nextDouble(min, max));
        };
    }

    // =========================================================
    // MAIN ENTRY (FRONTEND ENTRY POINT)
    // =========================================================

    public static PopulationBundle createProblem(
        ProblemType type,
        int populationSize,
        int size,
        GeneType geneType,
        int minGeneValue,
        int maxGeneValue,
        ProblemConfig config
    ) {

        return switch (type) {

            case GENERIC -> {
                Population population = createRandomPopulation(
                    populationSize,
                    size,
                    minGeneValue,
                    maxGeneValue,
                    geneType
                );

                yield new PopulationBundle(population, null);
            }

            case KNAPSACK -> {

                if (!(config instanceof KnapsackConfig kc)) {
                    throw new IllegalArgumentException("KnapsackConfig required");
                }

                KnapsackProblem problem = createKnapsackProblem(
                    size,
                    kc.getMinValue(),
                    kc.getMaxValue(),
                    kc.getMinWeight(),
                    kc.getMaxWeight(),
                    kc.getCapacity()
                );

                Population population = createKnapsackPopulation(
                    populationSize,
                    size
                );

                yield new PopulationBundle(population, problem);
            }

            case TSP -> {

                if (!(config instanceof TSPConfig tc)) {
                    throw new IllegalArgumentException("TSPConfig required");
                }

                TSPProblem problem = createTSPProblem(
                    size,
                    tc.getMinCoord(),
                    tc.getMaxCoord()
                );

                Population population = createTSPPopulation(
                    populationSize,
                    size
                );

                yield new PopulationBundle(population, problem);
            }
        };
    }
    // =========================================================
    // KNAPSACK
    // =========================================================

    public static KnapsackProblem createKnapsackProblem(
            int itemCount,
            double minValue,
            double maxValue,
            double minWeight,
            double maxWeight,
            double capacity
    ) {

        if (itemCount <= 0) {
            throw new IllegalArgumentException("itemCount must be > 0");
        }

        double[] values = new double[itemCount];
        double[] weights = new double[itemCount];

        for (int i = 0; i < itemCount; i++) {
            values[i] = RandomUtil.nextInt((int) Math.floor(minValue), (int) Math.ceil(maxValue));
            weights[i] = RandomUtil.nextInt((int) Math.floor(minWeight), (int) Math.ceil(maxWeight));
        }

        return new KnapsackProblem(values, weights, capacity);
    }

    public static Population createKnapsackPopulation(
            int populationSize,
            int itemCount
    ) {
        return createRandomPopulation(
                populationSize,
                itemCount,
                0,
                1,
                GeneType.BINARY
        );
    }

    // =========================================================
    // TSP
    // =========================================================

    public static TSPProblem createTSPProblem(
            int cityCount,
            double minCoord,
            double maxCoord
    ) {

        if (cityCount < 2) {
            throw new IllegalArgumentException("cityCount must be >= 2");
        }

        List<Point> points = new ArrayList<>();

        for (int i = 0; i < cityCount; i++) {
            double x = RandomUtil.nextInt((int) Math.floor(minCoord), (int) Math.ceil(maxCoord));
            double y = RandomUtil.nextInt((int) Math.floor(minCoord), (int) Math.ceil(maxCoord));
            points.add(new Point(x, y));
        }

        double[][] matrix = DistanceUtil.buildMatrix(points);

        return new TSPProblem(points, matrix);
    }

    public static Population createTSPPopulation(
            int populationSize,
            int cityCount
    ) {

        List<Individual> individuals = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {

            List<Integer> permutation = generatePermutation(cityCount);

            List<Gene> genes = permutation.stream()
                    .map(iVal -> (Gene) new IntegerGene(iVal))
                    .toList();

            individuals.add(new Individual(genes, "X" + (individuals.size() + 1)));
        }

        return new Population(individuals);
    }

    private static List<Integer> generatePermutation(int n) {

        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            list.add(i);
        }

        for (int i = n - 1; i > 0; i--) {
            int j = RandomUtil.nextInt(i + 1);

            int temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }

        return list;
    }
}