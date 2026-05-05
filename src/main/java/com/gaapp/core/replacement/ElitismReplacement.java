package com.gaapp.core.replacement;

import com.gaapp.core.model.Individual;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ElitismReplacement implements ReplacementStrategy {

    private final int eliteCount;

    public ElitismReplacement(int eliteCount) {
        this.eliteCount = eliteCount;
    }

    @Override
    public List<Individual> selectNextGeneration(
        List<Individual> currentPopulation,
        List<Individual> offspring,
        int populationSize
    ) {
        int N = populationSize;
        int K = offspring.size();
        int E = Math.min(eliteCount, N);

        // sort once (IMPORTANT: stable selection basis)
        List<Individual> sortedCurrent = currentPopulation.stream()
            .sorted(Comparator.comparingDouble(Individual::getFitness).reversed())
            .toList();

        List<Individual> sortedOffspring = offspring.stream()
            .sorted(Comparator.comparingDouble(Individual::getFitness).reversed())
            .toList();

        // -----------------------------
        // STEP 1: ELITE SELECTION
        // -----------------------------
        int eliteFromCurrent = Math.max(E, N - K);
        eliteFromCurrent = Math.min(eliteFromCurrent, N);

        List<Individual> elites = sortedCurrent.stream()
            .limit(eliteFromCurrent)
            .toList();

        for (Individual ind : elites) {
            ind.setType("elite");
        }

        // -----------------------------
        // STEP 2: OFFSPRING SELECTION
        // -----------------------------
        int offspringToTake = N - eliteFromCurrent;
        offspringToTake = Math.min(offspringToTake, K);

        List<Individual> selectedOffspring = sortedOffspring.stream()
            .limit(offspringToTake)
            .toList();

        for (Individual ind : selectedOffspring) {
            String mut = ind.getMutationAction();
            if (!mut.isBlank() && !mut.equalsIgnoreCase("No mutation")) {
                ind.setType("mutated");
            } else {
                ind.setType("offspring");
            }
        }

        // -----------------------------
        // STEP 3: MERGE
        // -----------------------------
        List<Individual> nextGen = new ArrayList<>(N);
        nextGen.addAll(elites);
        nextGen.addAll(selectedOffspring);

        return nextGen;
    }
}