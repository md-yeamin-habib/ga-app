package com.gaapp.core.replacement;

import com.gaapp.core.model.Individual;

import java.util.List;

public class FullReplacement implements ReplacementStrategy {

    @Override
    public List<Individual> selectNextGeneration(
            List<Individual> currentPopulation,
            List<Individual> offspring,
            int populationSize
    ) {
        int offspringSize = offspring.size();
        if (offspringSize == populationSize) {
            for (Individual ind : offspring) {
                String mut = ind.getMutationAction();
                if (!mut.isBlank() && !mut.equalsIgnoreCase("No mutation")) {
                    ind.setType("mutated");
                } else {
                    ind.setType("offspring");
                }
            }
            return offspring;
        } else {
            throw new IllegalStateException("Current population and offspring must be of the same size!");
        }
    }
}