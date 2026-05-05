package com.gaapp.core.engine;

import com.gaapp.core.model.Population;
import com.gaapp.core.model.Individual;
import com.gaapp.core.fitness.FitnessFunction;
import com.gaapp.core.selection.SelectionStrategy;
import com.gaapp.core.crossover.CrossoverStrategy;
import com.gaapp.core.engine.GeneticAlgorithmEngine.GAConfig;
import com.gaapp.core.mutation.MutationStrategy;
import com.gaapp.core.replacement.ReplacementStrategy;

import java.util.ArrayList;
import java.util.List;

public class GARunner {

    private final GeneticAlgorithmEngine engine;
    private final int maxGenerations;

    private final Double targetFitness;
    private final boolean applyTarget;

    private final int patience;
    private final double tolerance;
    private final boolean applyConvergence;

    private final boolean applyDiversity;
    private final double diversityThreshold;
    private final int diversityPatience;

    private final FitnessFunction fitness;
    private final SelectionStrategy selectionStrategy;
    private final CrossoverStrategy crossoverStrategy;
    private final MutationStrategy mutationStrategy;
    private final ReplacementStrategy replacementStrategy;

    private final double crossoverRate;
    private final int[] crossoverIndices;
    private final double mutationRate;
    private final int[] mutationIndices;

    public GARunner(
            GeneticAlgorithmEngine engine,
            int maxGenerations,
            Double targetFitness,
            boolean applyTarget,
            int patience,
            double tolerance,
            boolean applyConvergence,
            boolean applyDiversity,
            double diversityThreshold,
            int diversityPatience,
            FitnessFunction fitness,
            SelectionStrategy selectionStrategy,
            CrossoverStrategy crossoverStrategy,
            MutationStrategy mutationStrategy,
            ReplacementStrategy replacementStrategy,
            double crossoverRate,
            int[] crossoverIndices,
            double mutationRate,
            int[] mutationIndices
    ) {
        this.engine = engine;
        this.maxGenerations = maxGenerations;

        this.targetFitness = targetFitness;
        this.applyTarget = applyTarget;

        this.patience = patience;
        this.tolerance = tolerance;
        this.applyConvergence = applyConvergence;

        this.applyDiversity = applyDiversity;
        this.diversityThreshold = diversityThreshold;
        this.diversityPatience = diversityPatience;

        this.fitness = fitness;
        this.selectionStrategy = selectionStrategy;
        this.crossoverStrategy = crossoverStrategy;
        this.mutationStrategy = mutationStrategy;
        this.replacementStrategy = replacementStrategy;

        this.crossoverRate = crossoverRate;
        this.crossoverIndices = crossoverIndices.clone();
        this.mutationRate = mutationRate;
        this.mutationIndices = mutationIndices.clone();

        if (applyConvergence && patience <= 0) {
            throw new IllegalArgumentException("Patience must be > 0");
        }

        if (applyDiversity && diversityPatience <= 0) {
            throw new IllegalArgumentException("Diversity patience must be > 0");
        }
    }

    public List<GenerationResult> run(Population initialPopulation) {

        List<GenerationResult> history = new ArrayList<>();

        Population current = initialPopulation;

        GAConfig config = new GAConfig(
                fitness,
                selectionStrategy,
                crossoverStrategy,
                mutationStrategy,
                replacementStrategy,
                crossoverRate,
                crossoverIndices,
                mutationRate,
                mutationIndices
        );

        double bestFitness = Double.NEGATIVE_INFINITY;
        int stagnationCount = 0;
        int lowDiversityCount = 0;

        for (int gen = 0; gen < maxGenerations; gen++) {

            GenerationResult result = engine.runGenerationDetailed(current, config);
            double currentBest = result.getBestFitness();

            // Initialize baseline
            if (gen == 0) {
                bestFitness = currentBest;
            }

            // -------------------
            // TARGET STOP
            // -------------------
            if (applyTarget && targetFitness != null) {
                if (currentBest >= targetFitness) {
                    result.setStopCriteria("target");
                    history.add(result);
                    return history;
                }
            }

            // -------------------
            // CONVERGENCE STOP
            // -------------------
            if (applyConvergence) {

                if (currentBest > bestFitness + tolerance) {
                    bestFitness = currentBest;
                    stagnationCount = 0;
                } else {
                    stagnationCount++;
                }

                if (stagnationCount >= patience) {
                    result.setStopCriteria("convergence");
                    history.add(result);
                    return history;
                }
            }

            // -------------------
            // DIVERSITY STOP
            // -------------------
            if (applyDiversity) {

                double diversity = calculateDiversity(result.getNewPopulation());

                if (diversity < diversityThreshold) {
                    lowDiversityCount++;
                } else {
                    lowDiversityCount = 0;
                }

                if (lowDiversityCount >= diversityPatience) {
                    result.setStopCriteria("diversity");
                    history.add(result);
                    return history;
                }
            }

            history.add(result);

            current = result.getNewPopulation();
        }

        return history;
    }

    private double calculateDiversity(Population population) {

        long unique = population.getIndividuals().stream()
                .map(Individual::geneSignature) // IMPORTANT
                .distinct()
                .count();

        return unique * 1.0 / population.size();
    }
}
