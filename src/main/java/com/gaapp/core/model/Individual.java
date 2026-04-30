package com.gaapp.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gaapp.core.util.RandomUtil;

public class Individual {

    private List<Gene> genes;
    private double fitness;
    private String name;
    private double rouletteProbability;
    private double adjustedFitness;
    private double expectedCount;
    private int rank;
    private double rankProbability;
    private int weight;
    private Individual parent1;
    private Individual parent2;
    private String crossoverAction;
    private String mutationAction;
    private List<Gene> preMutation;

    public Individual(List<Gene> genes, String name) {
        this.genes = genes;
        this.name = name;
    }

    public List<Gene> getGenes() {
        return genes;
    }

    public void setGenes(List<Gene> genes) {
        this.genes = genes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim().toUpperCase();
    }

    public double getFitness() {
        return RandomUtil.round(fitness, 5);
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getRouletteProbability() {
        return RandomUtil.round(rouletteProbability, 5);
    }

    public void setRouletteSelection(double adjustedFitness, double rouletteProbability, double expectedCount) {
        this.rouletteProbability = rouletteProbability;
        this.adjustedFitness = adjustedFitness;
        this.expectedCount = expectedCount;
    }

    public double getAdjustedFitness() {
        return RandomUtil.round(adjustedFitness, 5);
    }

    public double getExpectedCount() {
        return RandomUtil.round(expectedCount, 5);
    }

    public int getRank() {
        return rank;
    }

    public int getWeight() {
        return weight;
    }

    public double getRankProbability() {
        return RandomUtil.round(rankProbability, 5);
    }

    public void setRankSelection(int rank, int weight, double rankProbability) {
        this.rankProbability = rankProbability;
        this.rank = rank;
        this.weight = weight;
    }

    public Individual getParent1() {
        return parent1;
    }

    public Individual getParent2() {
        return parent2;
    }

    public String getCrossoverAction() {
        return crossoverAction;
    }

    public void setCrossover(Individual parent1, Individual parent2, String action) {
        this.parent1 = parent1;
        this.parent2 = parent2;
        this.crossoverAction = action;
    }

    public List<Gene> getPreMutation() {
        return preMutation;
    }

    public String getMutationAction() {
        return mutationAction;
    }

    public void setMutation(List<Gene> original, String action) {
        this.preMutation = original;
        this.mutationAction = action;
    }

    public List<Gene> copyGenes() {
        List<Gene> copy = new ArrayList<>();
        for (Gene g : this.genes) {
            copy.add(g.copy());
        }
        return copy;
    }

    public Individual copy() {
        Individual clone = new Individual(copyGenes(), this.name);
        clone.setFitness(this.fitness);
        clone.setRouletteSelection(this.adjustedFitness, this.rouletteProbability, this.expectedCount);
        clone.setRankSelection(this.rank, this.weight, this.rankProbability);
        clone.setCrossover(this.parent1, this.parent2, this.crossoverAction);
        clone.setMutation(this.preMutation, this.mutationAction); 
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(geneSignature());
        double f = RandomUtil.round(fitness, 5);
        sb.append(" | Fitness: ").append(f);
        return sb.toString();
    }

    public String geneSignature() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < genes.size(); i++) {
            Gene g = genes.get(i);
            sb.append(g.toString());
            if (i < genes.size() - 1) {
                sb.append(" - ");  
            }
        }
        return sb.toString();
    }

    public boolean isValidPermutation() {
        if (genes == null || genes.isEmpty()) return false;

        Set<Integer> seen = new HashSet<>();

        for (Gene g : genes) {
            Object val = g.getValue();
            if (!(val instanceof Number)) return false;
            int num = ((Number) val).intValue();
            if (seen.contains(num)) return false;
            seen.add(num);
        }

        return true;
    }

    public void setGene(int index, Gene gene) {
        genes.set(index, gene);
    }

    public void invalidateFitness() {
        this.fitness = 0.0;
    }
}