package com.gaapp.core.replacement;

import com.gaapp.core.model.Individual;

import java.util.List;

public interface ReplacementStrategy {

    List<Individual> selectNextGeneration(
        List<Individual> currentPopulation,
        List<Individual> offspring,
        int populationSize
    );
}