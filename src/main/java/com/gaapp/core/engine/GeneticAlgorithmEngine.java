package com.gaapp.core.engine;

import com.gaapp.core.model.Individual;
import com.gaapp.core.model.Population;

import com.gaapp.core.fitness.FitnessFunction;
import com.gaapp.core.selection.SelectionStrategy;
import com.gaapp.core.crossover.CrossoverStrategy;
import com.gaapp.core.mutation.MutationStrategy;
import com.gaapp.core.replacement.ReplacementStrategy;

import java.util.ArrayList;
import java.util.List;

public class GeneticAlgorithmEngine {

    public GenerationResult runGenerationDetailed(Population population, GAConfig config) {

        population.evaluate(config.fitness);
        int N = population.size();
        validatePopulation(population);

        List<Individual> before = copy(population.getIndividuals());

        // Selection
        List<Individual> parents =
                config.selectionStrategy.select(before, N);
        List<Individual> parentsCopy = copy(parents);

        // Crossover
        List<Individual> offspring =
                applyCrossover(config.crossoverStrategy, parents,
                        config.crossoverRate, config.crossoverIndices, N);

        List<Individual> afterCross = evaluateAndCopy(config.fitness, offspring);
        List<Double> afterCrossFitness = extractFitness(afterCross);

        // Mutation
        applyMutation(config.mutationStrategy, offspring,
                config.mutationRate, config.mutationIndices, N);

        List<Individual> afterMut = evaluateAndCopy(config.fitness, offspring);
        List<Double> afterMutFitness = extractFitness(afterMut);

        // Replacement
        List<Individual> nextGen =
                config.replacementStrategy.selectNextGeneration(before, afterMut, N);

        List<Individual> displayNextGen = copy(nextGen);
        List<Individual> runtimeNextGen = copy(nextGen);

        Population newPopulation = new Population(runtimeNextGen);
        validatePopulation(newPopulation);
        newPopulation.evaluate(config.fitness);

        int i = 1;
        for (Individual ind : runtimeNextGen) {
            ind.setName("X" + i++);
        }

        return new GenerationResult(
                before,
                parentsCopy,
                afterCross,
                afterCrossFitness,
                afterMut,
                afterMutFitness,
                displayNextGen,
                newPopulation
        );
    }

    // =========================================================
    // INTERNAL HELPERS
    // =========================================================

    private List<Individual> applyCrossover(
            CrossoverStrategy strategy,
            List<Individual> parents,
            double rate,
            int[] indices,
            int N
    ) {

        List<Individual> offspring = new ArrayList<>();

        for (int i = 0; i < parents.size(); i += 2) {

            Individual p1 = parents.get(i);

            if (i + 1 < parents.size()) {
                Individual p2 = parents.get(i + 1);

                offspring.addAll(
                        strategy.crossover(p1, p2, rate, indices)
                );
            } else {
                Individual copied = new Individual(p1.getGenes(), "");
                copied.setCrossover(p1, p1, "Carried Over");
                offspring.add(copied);
            }
        }

        if (offspring.size() > N) {
                return offspring.subList(0, N);
        }

        int counter = 1;
        for (Individual ind : offspring) {
            ind.setName("O" + counter++);
        }

        validateSize(offspring, N, "Crossover");
        return offspring;
    }

    private void applyMutation(
            MutationStrategy strategy,
            List<Individual> list,
            double rate,
            int[] indices,
            int N
    ) {

        validateSize(list, N, "Mutation");
        for (Individual ind : list) {
            strategy.mutate(ind, rate, indices);
        }
    }

    private List<Individual> evaluateAndCopy(
            FitnessFunction fitness,
            List<Individual> list
    ) {

        List<Individual> out = new ArrayList<>();

        for (Individual ind : list) {
            Individual copy = ind.copy();
            copy.setFitness(fitness.evaluate(copy));
            out.add(copy);
        }

        return out;
    }

    private List<Double> extractFitness(List<Individual> list) {
        return list.stream().map(Individual::getFitness).toList();
    }

    private List<Individual> copy(List<Individual> list) {
        return list.stream().map(Individual::copy).toList();
    }

    // =========================================================
    // CONFIG OBJECT (IMPORTANT)
    // =========================================================

    public static class GAConfig {

        public final FitnessFunction fitness;
        public final SelectionStrategy selectionStrategy;
        public final CrossoverStrategy crossoverStrategy;
        public final MutationStrategy mutationStrategy;
        public final ReplacementStrategy replacementStrategy;

        public final double crossoverRate;
        public final int[] crossoverIndices;
        public final double mutationRate;
        public final int[] mutationIndices;

        public GAConfig(
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
            this.fitness = fitness;
            this.selectionStrategy = selectionStrategy;
            this.crossoverStrategy = crossoverStrategy;
            this.mutationStrategy = mutationStrategy;
            this.replacementStrategy = replacementStrategy;
            this.crossoverRate = crossoverRate;
            this.crossoverIndices = crossoverIndices;
            this.mutationRate = mutationRate;
            this.mutationIndices = mutationIndices;
        }
    }

    private void validateSize(List<Individual> list, int N, String stage) {
        if (list.size() < 1) {
            throw new IllegalStateException(stage + " must have at least one individual");
        }
        if (list.size() > N) {
            throw new IllegalStateException(stage + " cannot exceed population size");
        }
    }

    private void validatePopulation(Population pop) {
        if (pop.size() < 2) {
                throw new IllegalStateException("Population must have at least 2 individuals");
        }
    }
}