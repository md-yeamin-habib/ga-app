package com.gaapp;

import com.gaapp.core.engine.*;
import com.gaapp.core.factory.*;
import com.gaapp.core.fitness.*;
import com.gaapp.core.model.*;
import com.gaapp.core.problem.*;
import com.gaapp.core.util.RandomUtil;
import com.gaapp.core.selection.SelectionStrategy;
import com.gaapp.core.crossover.CrossoverStrategy;
import com.gaapp.core.mutation.MutationStrategy;
import com.gaapp.core.replacement.ReplacementStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // -----------------------------
        // BASIC GA CONFIG
        // -----------------------------
        System.out.println("=== GENETIC ALGORITHM CONFIG ===");

        System.out.print("Population Size: ");
        int populationSize = sc.nextInt();

        System.out.print("Gene Length: ");
        int geneLength = sc.nextInt();

        System.out.println("Gene Type: 1-BINARY 2-INTEGER 3-REAL");
        PopulationFactory.GeneType geneType =
                PopulationFactory.GeneType.values()[sc.nextInt() - 1];

        int minGeneValue = 0;
        int maxGeneValue = 9;

        if (geneType == PopulationFactory.GeneType.INTEGER ||
            geneType == PopulationFactory.GeneType.REAL) {

            System.out.print("Min Gene Value: ");
            minGeneValue = sc.nextInt();

            System.out.print("Max Gene Value: ");
            maxGeneValue = sc.nextInt();
        }

        // -----------------------------
        // PROBLEM TYPE
        // -----------------------------
        System.out.println("Problem Type: 1-GENERIC 2-KNAPSACK 3-TSP");
        ProblemType problemType =
                ProblemType.values()[sc.nextInt() - 1];

        ProblemConfig problemConfig = null;

        switch (problemType) {

            case KNAPSACK -> {
                System.out.print("Min Value: ");
                double minValue = sc.nextDouble();

                System.out.print("Max Value: ");
                double maxValue = sc.nextDouble();

                System.out.print("Min Weight: ");
                double minWeight = sc.nextDouble();

                System.out.print("Max Weight: ");
                double maxWeight = sc.nextDouble();

                System.out.print("Capacity: ");
                double capacity = sc.nextDouble();

                problemConfig = new KnapsackConfig(
                        minValue, maxValue,
                        minWeight, maxWeight,
                        capacity
                );

                geneType = PopulationFactory.GeneType.BINARY;
            }

            case TSP -> {
                System.out.print("Min Coordinate: ");
                double minCoord = sc.nextDouble();

                System.out.print("Max Coordinate: ");
                double maxCoord = sc.nextDouble();

                problemConfig = new TSPConfig(minCoord, maxCoord);

                geneType = PopulationFactory.GeneType.INTEGER;
            }

            case GENERIC -> {
                // no config needed
            }
        }

        // -----------------------------
        // SEED INPUT
        // -----------------------------
        sc.nextLine(); // clear buffer

        System.out.print("Seed (press Enter for random): ");
        String seedInput = sc.nextLine();

        long seed;

        if (seedInput.isBlank()) {
                seed = RandomUtil.setRandomSeed(); // generate random seed
        } else {
                seed = Long.parseLong(seedInput);
                RandomUtil.setSeed(seed);
        }

        System.out.println("Using Seed: " + seed);

        System.out.print("Max Generations: ");
        int maxGenerations = sc.nextInt();

        System.out.print("\nApply Target Fitness? ");
        boolean applyTarget = sc.nextBoolean();

        double targetFitness;
        if (applyTarget) {

                System.out.print("Target Fitness: ");
                targetFitness = sc.nextDouble();
        }
        else {
                targetFitness = Double.MAX_VALUE;
        }


        System.out.print("Apply Convergence? ");
        boolean applyConvergence = sc.nextBoolean();

        double finalTolerance;
        int finalPatience;

        if (applyConvergence) {
                System.out.println("\nEnter tolerance and patience for early stopping (optional, press Enter to skip): ");
                sc.nextLine(); // clear buffer
                System.out.print("Tolerance: ");
                String toleranceStr = sc.nextLine();

                if (toleranceStr.isBlank() || Double.parseDouble(toleranceStr) <= 0) {
                        finalTolerance = 1e-6; // default small value
                } else {
                        finalTolerance = Double.parseDouble(toleranceStr);
                }

                sc.nextLine(); // clear buffer
                System.out.print("Patience: ");
                String patienceStr = sc.nextLine();

                if (patienceStr.isBlank() || Integer.parseInt(patienceStr) <= 0) {
                        finalPatience = 10; // default patience
                } else {
                        finalPatience = Integer.parseInt(patienceStr);
                }
        } else {
                finalPatience = Integer.MAX_VALUE;
                finalTolerance = Double.MAX_VALUE;
        }

        System.out.print("Apply Diversity? ");
        boolean applyDiversity = sc.nextBoolean();

        double diversityThreshold;
        int diversityPatience;

        if (applyDiversity) {
                System.out.println("\nEnter threshold and patience for early stopping (optional, press Enter to skip): ");
                sc.nextLine(); // clear buffer
                System.out.print("Diversity Threshold: ");
                String threshold = sc.nextLine();

                if (threshold.isBlank() || Double.parseDouble(threshold) <=0) {
                        diversityThreshold = 0.3;
                } else {
                        diversityThreshold = Double.parseDouble(threshold);
                }

                sc.nextLine();
                System.out.print("Diversity Patience: ");
                String patience = sc.nextLine();

                if (patience.isBlank() || Integer.parseInt(patience) <=0) {
                        diversityPatience = 3;
                } else {
                        diversityPatience = Integer.parseInt(patience);
                }
        } else {
                diversityThreshold = Double.MAX_VALUE;
                diversityPatience = Integer.MAX_VALUE;
        }

        // -----------------------------
        // FITNESS TYPE
        // -----------------------------
        System.out.println("Fitness: 1-SUM 2-BINARY 3-ONES 4-TSP 5-KNAPSACK 6-CUSTOM");
        FitnessFactory.FitnessType fitnessType =
                FitnessFactory.FitnessType.values()[sc.nextInt() - 1];

        FitnessConfig fitnessConfig = null;

        switch (fitnessType) {

            case TSP -> {
                TSPProblem tsp = (TSPProblem) PopulationFactory
                        .createProblem(problemType, populationSize, geneLength,
                                geneType, minGeneValue, maxGeneValue, problemConfig)
                        .getProblemData();

                fitnessConfig = new TSPFitnessConfig(tsp.getDistanceMatrix());
            }

            case KNAPSACK -> {
                KnapsackProblem kp = (KnapsackProblem) PopulationFactory
                        .createProblem(problemType, populationSize, geneLength,
                                geneType, minGeneValue, maxGeneValue, problemConfig)
                        .getProblemData();

                fitnessConfig = new KnapsackFitnessConfig(
                        kp.getValues(),
                        kp.getWeights(),
                        kp.getCapacity()
                );
            }

            case CUSTOM -> {
                sc.nextLine();
                System.out.print("Enter expression (e.g. x0 + x1^2): ");
                String expr = sc.nextLine();

                fitnessConfig = new CustomFitnessConfig(expr);
            }

            default -> {
                // SUM / BINARY / ONES need no config
            }
        }

        // -----------------------------
        // SELECTION
        // -----------------------------
        System.out.println("Selection: 1-ROULETTE 2-RANK 3-TOURNAMENT 4-DIRECT_PICK");
        SelectionFactory.SelectionType selectionType =
                SelectionFactory.SelectionType.values()[sc.nextInt() - 1];

        int tournamentSize = 3;
        if (selectionType == SelectionFactory.SelectionType.TOURNAMENT) {
            System.out.print("Tournament Size: ");
            tournamentSize = sc.nextInt();
        }

        // -----------------------------
        // CROSSOVER
        // -----------------------------
        System.out.println("Crossover: 1-ONE_POINT 2-TWO_POINT 3-ORDER");
        CrossoverFactory.CrossoverType crossoverType =
                CrossoverFactory.CrossoverType.values()[sc.nextInt() - 1];

        System.out.print("Crossover Rate: ");
        double crossoverRate = sc.nextDouble();

        System.out.print("Crossover Indices (comma-separated): ");
        int[] crossoverIndices = Arrays.stream(sc.next().split(","))
                .mapToInt(Integer::parseInt)
                .toArray();

        // -----------------------------
        // MUTATION
        // -----------------------------
        System.out.println("Mutation: 1-BIT_FLIP 2-RANDOM_RESET 3-SWAP 4-INVERSION 5-SCRAMBLE");
        MutationFactory.MutationType mutationType =
                MutationFactory.MutationType.values()[sc.nextInt() - 1];

        System.out.print("Mutation Rate: ");
        double mutationRate = sc.nextDouble();

        System.out.print("Mutation Indices (comma-separated): ");
        int[] mutationIndices = Arrays.stream(sc.next().split(","))
                .mapToInt(Integer::parseInt)
                .toArray();

        // -----------------------------
        // REPLACEMENT
        // -----------------------------
        System.out.println("Replacement: 1-FULL 2-ELITISM");
        ReplacementFactory.ReplacementType replacementType =
                ReplacementFactory.ReplacementType.values()[sc.nextInt() - 1];

        int elitismCount = 2;
        if (replacementType == ReplacementFactory.ReplacementType.ELITISM) {
            System.out.print("Elitism Count: ");
            elitismCount = sc.nextInt();
        }


        // -----------------------------
        // POPULATION + PROBLEM
        // -----------------------------
        PopulationBundle bundle = PopulationFactory.createProblem(
                problemType,
                populationSize,
                geneLength,
                geneType,
                minGeneValue,
                maxGeneValue,
                problemConfig
        );

        Population population = bundle.getPopulation();
        ProblemData problemData = (ProblemData) bundle.getProblemData();

        if (problemData != null) {
                problemData.printDebug();
        }

        // -----------------------------
        // FITNESS
        // -----------------------------
        FitnessFunction fitness = FitnessFactory.create(
                fitnessType,
                problemData,
                geneType,
                fitnessConfig,
                geneLength
        );

        // -----------------------------
        // STRATEGIES
        // -----------------------------
        SelectionStrategy selection =
                SelectionFactory.create(selectionType, tournamentSize).getStrategy();

        CrossoverStrategy crossover =
                CrossoverFactory.create(crossoverType).getStrategy();

        MutationStrategy mutation =
                MutationFactory.create(mutationType, minGeneValue, maxGeneValue).getStrategy();

        ReplacementStrategy replacement =
                ReplacementFactory.create(replacementType, elitismCount).getStrategy();

        // -----------------------------
        // ENGINE
        // -----------------------------
        GeneticAlgorithmEngine engine = new GeneticAlgorithmEngine();

        GARunner runner = new GARunner(
                engine, 
                maxGenerations, 
                targetFitness,
                applyTarget,
                finalPatience,
                finalTolerance,
                applyConvergence,
                applyDiversity,
                diversityThreshold,
                diversityPatience,
                fitness,
                selection,
                crossover,
                mutation,
                replacement,
                crossoverRate,
                crossoverIndices,
                mutationRate,
                mutationIndices
        );

        // -----------------------------
        // RUN
        // -----------------------------
        List<GenerationResult> history = runner.run(population);

        // -----------------------------
        // OUTPUT
        // -----------------------------
        int genNum = 1;

        for (GenerationResult gen : history) {

            System.out.println("\n==============================");
            System.out.println("GENERATION " + genNum++);
            System.out.println("==============================");

            System.out.println("\nPopulation BEFORE:");
            gen.getPopulationBefore().forEach(System.out::println);

            System.out.println("\nSelected Parents:");
            gen.getSelectedParents().forEach(System.out::println);

            System.out.println("\nAfter Crossover:");
            gen.getAfterCrossover().forEach(System.out::println);

            System.out.println("\nAfter Mutation:");
            gen.getAfterMutation().forEach(System.out::println);

            System.out.println("\nNext Generation:");
            gen.getNextGeneration().forEach(System.out::println);

            System.out.println("\nBest Fitness: " + gen.getBestFitness());
            System.out.println("Average Fitness: " + gen.getAvgFitness());
        }

        System.out.println("\n=== FINAL BEST ===");

        GenerationResult last = history.get(history.size() - 1);

        last.getNextGeneration().stream()
                .max((a, b) -> Double.compare(a.getFitness(), b.getFitness()))
                .ifPresent(System.out::println);

        String reason = last.getStopCriteria();

        if (history.size() < maxGenerations && reason != null) {

                if (applyConvergence && reason.equals("convergence")) {
                        System.out.println("Stopped at generation " + history.size() + " due to no improvement for " + finalPatience + " generations.");
                }

                if (applyDiversity && reason.equals("diversity")) {
                        System.out.println("Stopped at generation " + history.size() + " due to loss in diversity for " + diversityPatience + " generations.");
                }

                if (applyTarget && last.getBestFitness() >= targetFitness && reason.equals("target")) {
                        System.out.println("Target fitness " + targetFitness + " reached in generation " + history.size());
                }

        } else {

                if (applyTarget) {
                        System.out.println("Max generations reached without hitting target.");
                } else {
                        System.out.println("Max generations reached.");
                }
        }
        
        System.out.println("\n=== END OF RUN ===");
    }
}