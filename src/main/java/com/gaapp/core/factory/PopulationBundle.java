package com.gaapp.core.factory;

import com.gaapp.core.model.Population;
import com.gaapp.core.problem.ProblemData;

public class PopulationBundle {

    private final Population population;
    private final ProblemData problemData;

    public PopulationBundle(Population population, ProblemData problemData) {
        this.population = population;
        this.problemData = problemData;
    }

    public Population getPopulation() { return population; }

    public ProblemData getProblemData() { return problemData; }
}