package com.gaapp.core.mutation;

import com.gaapp.core.model.Individual;

public interface MutationStrategy {
    void mutate(Individual individual, double mutationRate, int... params);
}

