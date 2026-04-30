package com.gaapp.dto;

import com.gaapp.dto.TableDTO;
import java.util.List;

public class GenerationDTO {

    private int generation;

    // Tables
    private TableDTO populationBefore;   // N.1
    private TableDTO afterCrossover;     // N.2
    private TableDTO afterMutation;      // N.3
    private TableDTO nextGeneration;     // (N+1).1
    private TableDTO newPopulation;      // runtime

    // Fitness columns (optional but useful)
    private List<Double> crossoverFitness;
    private List<Double> mutationFitness;

    // Summary
    private double bestFitness;
    private double avgFitness;
    private String bestIndividual;

    private String stopCriteria;

    // getters + setters
    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public TableDTO getPopulationBefore() {
        return populationBefore;
    }

    public void setPopulationBefore(TableDTO populationBefore) {
        this.populationBefore = populationBefore;
    }

    public TableDTO getAfterCrossover() {
        return afterCrossover;
    }

    public void setAfterCrossover(TableDTO afterCrossover) {
        this.afterCrossover = afterCrossover;
    }

    public TableDTO getAfterMutation() {
        return afterMutation;
    }

    public void setAfterMutation(TableDTO afterMutation) {
        this.afterMutation = afterMutation;
    }

    public TableDTO getNextGeneration() {
        return nextGeneration;
    }

    public TableDTO getNewPopulation() {
        return newPopulation;
    }

    public void setNewPopulation(TableDTO newPopulation) {
        this.newPopulation = newPopulation;
    }

    public void setNextGeneration(TableDTO nextGeneration) {
        this.nextGeneration = nextGeneration;
    }

    public List<Double> getCrossoverFitness() {
        return crossoverFitness;
    }

    public void setCrossoverFitness(List<Double> crossoverFitness) {
        this.crossoverFitness = crossoverFitness;
    }

    public List<Double> getMutationFitness() {
        return mutationFitness;
    }

    public void setMutationFitness(List<Double> mutationFitness) {
        this.mutationFitness = mutationFitness;
    }

    public double getBestFitness() {
        return bestFitness;
    }

    public void setBestFitness(double bestFitness) {
        this.bestFitness = bestFitness;
    }

    public double getAvgFitness() {
        return avgFitness;
    }

    public void setAvgFitness(double avgFitness) {
        this.avgFitness = avgFitness;
    }

    public String getBestIndividual() {
        return bestIndividual;
    }

    public void setBestIndividual(String bestIndividual) {
        this.bestIndividual = bestIndividual;
    }

    public String getStopCriteria() {
        return stopCriteria;
    }

    public void setStopCriteria(String stopCriteria) {
        this.stopCriteria = stopCriteria;
    }

}