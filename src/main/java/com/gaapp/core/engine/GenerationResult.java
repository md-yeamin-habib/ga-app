package com.gaapp.core.engine;

import com.gaapp.core.model.Individual;
import com.gaapp.core.model.Population;

import java.util.ArrayList;
import java.util.List;

public class GenerationResult {

    private final List<Individual> populationBefore;
    private final List<Individual> selectedParents;
    private final List<Individual> afterCrossover;
    private final List<Double> afterCrossoverFitness;
    private final List<Individual> afterMutation;
    private final List<Double> afterMutationFitness;
    private final List<Individual> nextGeneration;
    private final Population newPopulation;

    private String stopCriteria = "maxgen";
    private int eliteCount = 0;

    public GenerationResult(
            List<Individual> populationBefore,
            List<Individual> selectedParents,
            List<Individual> afterCrossover,
            List<Double> afterCrossoverFitness,
            List<Individual> afterMutation,
            List<Double> afterMutationFitness,
            List<Individual> nextGeneration,
            Population newPopulation
    ) {
        this.populationBefore = populationBefore;
        this.selectedParents = selectedParents;
        this.afterCrossover = afterCrossover;
        this.afterCrossoverFitness = afterCrossoverFitness;
        this.afterMutation = afterMutation;
        this.afterMutationFitness = afterMutationFitness;
        this.nextGeneration = nextGeneration;
        this.newPopulation = newPopulation;
    }

    public List<Individual> getPopulationBefore() { return populationBefore; }
    public List<Individual> getSelectedParents() { return selectedParents; }
    public List<Individual> getAfterCrossover() { return afterCrossover; }
    public List<Double> getAfterCrossoverFitness() { return afterCrossoverFitness; }
    public List<Double> getAfterMutationFitness() { return afterMutationFitness; }
    public List<Individual> getAfterMutation() { return afterMutation; }
    public List<Individual> getNextGeneration() { return nextGeneration; }
    public Population getNewPopulation() { return newPopulation; }

    public double getBestFitness() { return newPopulation.getBestFitness(); }
    public double getAvgFitness() { return newPopulation.getAverageFitness(); }
    
    public String getBestIndividual() {
        Individual best = newPopulation.getBestIndividual();
        return best != null ? best.geneSignature() : "";
    }

    public String getStopCriteria() { return stopCriteria; }
    public void setStopCriteria(String str) {
        stopCriteria = str;
    }

    public List<Double> getRouletteProbabilities() {
        List<Double> probs = new ArrayList<>();

        for (Individual ind : selectedParents) {
            probs.add(ind.getRouletteProbability());
        }

        return probs;
    }

    public List<Double> getRankProbabilities() {
        List<Double> probs = new ArrayList<>();

        for (Individual ind : selectedParents) {
            probs.add(ind.getRankProbability());
        }

        return probs;
    }

    public int getEliteCount() {
        return eliteCount;
    }

    public void setEliteCount(int eliteCount) {
        this.eliteCount = eliteCount;
    }


    public int getMutatedCount() {
        int count = 0;

        for (Individual ind : afterMutation) {
            if (ind.getMutationAction() != null &&
                !ind.getMutationAction().equalsIgnoreCase("No mutation")) {
                count++;
            }
        }

        return count;
    }

    public int getNonMutatedCount() {
        return afterMutation.size() - getMutatedCount();
    }

    public int getOffspringCount() {
        return nextGeneration.size() - getEliteCount();
    }
}