package com.gaapp.core.selection;

import com.gaapp.core.model.Individual;
import com.gaapp.core.model.Population;
import com.gaapp.core.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;

public class TournamentSelection implements SelectionStrategy {

    private final int tournamentSize;

    public TournamentSelection(int tournamentSize) {
        this.tournamentSize = tournamentSize;
    }

    @Override
    public List<Individual> select(List<Individual> population, int count) {

        List<Individual> individuals = population;

        int N = population.size();

        if (count > N) {
            throw new IllegalArgumentException("Selection count cannot exceed population size");
        }


        if (individuals.isEmpty()) {
            throw new IllegalArgumentException("Population is empty");
        }

        List<Individual> result = new ArrayList<>();

        int actualSize = Math.min(tournamentSize, individuals.size());

        for (int i = 0; i < count; i++) {

            // 1. build tournament subset
            List<Individual> contenders = new ArrayList<>();

            for (int j = 0; j < actualSize; j++) {
                int idx = RandomUtil.nextInt(individuals.size());
                contenders.add(individuals.get(idx));
            }

            // 2. pick best from subset
            Individual best = null;

            for (Individual ind : contenders) {
                if (best == null || ind.getFitness() > best.getFitness()) {
                    best = ind;
                }
            }

            result.add(best.copy());
        }

        return result;
    }
}