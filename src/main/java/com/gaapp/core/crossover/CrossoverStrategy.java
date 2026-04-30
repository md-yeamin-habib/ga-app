package com.gaapp.core.crossover;

import com.gaapp.core.model.Individual;

import java.util.List;

public interface CrossoverStrategy {
    List<Individual> crossover(Individual p1, Individual p2, double crossoverRate, int... indices);
}