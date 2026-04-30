package com.gaapp.core.selection;

import com.gaapp.core.model.Individual;

import java.util.List;
import java.util.stream.Collectors;

public class DirectPickSelection implements SelectionStrategy {

    @Override
    public List<Individual> select(List<Individual> population, int count) {

        int N = population.size();

        if (count > N) {
            throw new IllegalArgumentException("Selection count cannot exceed population size");
        }

        return population.stream()
                .limit(count)
                .map(Individual::copy)
                .collect(Collectors.toList());
    }
}