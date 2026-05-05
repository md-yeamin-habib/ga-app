package com.gaapp.core.model;

import com.gaapp.core.fitness.FitnessFunction;
import com.gaapp.core.util.RandomUtil;

import java.util.List;

public class Population {

    private List<Individual> individuals;

    public Population(List<Individual> individuals) {
        this.individuals = individuals;
    }

    public List<Individual> getIndividuals() {
        return individuals;
    }

    public void evaluate(FitnessFunction fitnessFunction) {
        for (Individual ind : individuals) {
            double fitness = fitnessFunction.evaluate(ind);
            fitness = RandomUtil.round(fitness, 5);
            ind.setFitness(fitness);
        }
    }

    public Individual getBestIndividual() {
        return individuals.stream()
                .max((a, b) -> Double.compare(a.getFitness(), b.getFitness()))
                .orElse(null);
    }

    public double getBestFitness() {
        return individuals.stream()
                .mapToDouble(Individual::getFitness)
                .max()
                .orElse(0.0);
    }

    public double getAverageFitness() {
        double avg = individuals.stream()
                .mapToDouble(Individual::getFitness)
                .average()
                .orElse(0.0);
        return RandomUtil.round(avg, 5);
    }

    public double getWorstFitness() {
        return individuals.stream()
                .mapToDouble(Individual::getFitness)
                .min()
                .orElse(0.0);
    }

    public List<Individual> getTopK(int k) {

        if (individuals == null || individuals.isEmpty()) {
            throw new IllegalArgumentException("Population is empty");
        }

        k = Math.min(k, individuals.size());

        return individuals.stream()
                .sorted((a, b) -> Double.compare(b.getFitness(), a.getFitness())) // descending
                .limit(k)
                .map(Individual::copy)
                .toList();
    }

    public int size() {
        return individuals.size();
    }

    public void addIndividual(Individual ind) {
        if (ind == null) {
            throw new IllegalArgumentException("Individual cannot be null");
        }
        individuals.add(ind);
    }

    public void removeIndividual(int index) {
        if (individuals.size() <= 2) {
            throw new IllegalStateException("Population must have at least 2 individuals");
        }
        individuals.remove(index);
    }

    public void updateIndividual(int index, Individual newInd) {
        if (newInd == null) {
            throw new IllegalArgumentException("Individual cannot be null");
        }
        individuals.set(index, newInd);
    }

    public void setIndividuals(List<Individual> newList) {
        if (newList == null || newList.size() < 2) {
            throw new IllegalArgumentException("Population must have at least 2 individuals");
        }
        this.individuals = newList;
    }
}