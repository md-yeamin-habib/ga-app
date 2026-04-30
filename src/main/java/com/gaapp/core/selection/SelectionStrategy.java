package com.gaapp.core.selection;

import com.gaapp.core.model.Individual;
import com.gaapp.core.model.Population;

import java.util.List;

public interface SelectionStrategy {
    List<Individual> select(List<Individual> population, int count);
}