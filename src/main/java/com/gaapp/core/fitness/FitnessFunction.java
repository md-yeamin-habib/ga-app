package com.gaapp.core.fitness;

import com.gaapp.core.model.Individual;

public interface FitnessFunction {
    double evaluate(Individual individual);
}