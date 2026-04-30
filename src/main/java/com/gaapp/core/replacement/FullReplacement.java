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
            return offspring;
        } else {
            throw new IllegalStateException("Current population and offspring must be of the same size!");
        }
    }
}