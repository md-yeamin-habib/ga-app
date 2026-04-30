package com.gaapp.service;

import java.util.*;
import java.util.stream.Collectors;

public class GARequest {

    // ==========================
    // INPUT DATA (FROM UI TABLES)
    // ==========================
    private Map<String, List<List<String>>> tables = Collections.emptyMap();

    // ==========================
    // BASIC CONFIG
    // ==========================
    private String problemType;
    private int populationSize;
    private int geneLength;
    private String geneType;

    private int minGene;
    private int maxGene;

    // ==========================
    // PROBLEM-SPECIFIC CONFIG
    // ==========================
    private int minWeight;
    private int maxWeight;
    private int minValue;
    private int maxValue;
    private int capacity;

    private int minCoord;
    private int maxCoord;

    // ==========================
    // FITNESS
    // ==========================
    private String fitnessType;

    private String customFormula;

    // ==========================
    // OPERATORS
    // ==========================
    private String selectionType;
    private int tournamentSize;

    private String crossoverType;
    private double crossoverRate;
    private String crossoverIndices; // raw string from frontend

    private String mutationType;
    private double mutationRate;
    private String mutationIndices; // raw string from frontend

    private String replacementType;
    private int eliteCount;

    // ==========================
    // STOPPING / CONTROL
    // ==========================
    private int maxGenerations;
    private Long seed;

    private boolean applyTarget;
    private double targetFitness;

    private boolean applyConvergence;
    private double convergenceTolerance;
    private int convergencePatience;

    private boolean applyDiversity;
    private double diversityThreshold;
    private int diversityPatience;

    // ==========================
    // SAFE PARSERS (IMPORTANT FIX)
    // ==========================

    private List<Integer> parseIndices(String input) {

        if (input == null) return Collections.emptyList();

        input = input.trim().toLowerCase();

        if (input.isEmpty() || input.equals("random")) {
            return Collections.emptyList(); // backend will randomize
        }

        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    // ==========================
    // DERIVED ACCESSORS (IMPORTANT)
    // ==========================

    public List<Integer> getCrossoverPoints() {
        return parseIndices(crossoverIndices);
    }

    public List<Integer> getMutationPoints() {
        return parseIndices(mutationIndices);
    }

    public String getCustomFormula() {
        return customFormula;
    }

    public boolean isCustomFitness() {
        return "CUSTOM".equalsIgnoreCase(fitnessType);
    }

    // ==========================
    // TABLE ACCESS
    // ==========================
    public Map<String, List<List<String>>> getTables() {
        return tables;
    }

    public List<List<String>> getTable(String key) {
        return tables.getOrDefault(key, Collections.emptyList());
    }

    public boolean hasTables() {
        return tables != null && !tables.isEmpty();
    }

    // ==========================
    // GETTERS (UNCHANGED CORE)
    // ==========================
    public String getProblemType() { return problemType; }
    public int getPopulationSize() { return populationSize; }
    public int getGeneLength() { return geneLength; }
    public String getGeneType() { return geneType; }

    public int getMinGene() { return minGene; }
    public int getMaxGene() { return maxGene; }

    public int getMinWeight() { return minWeight; }
    public int getMaxWeight() { return maxWeight; }
    public int getMinValue() { return minValue; }
    public int getMaxValue() { return maxValue; }
    public int getCapacity() { return capacity; }

    public int getMinCoord() { return minCoord; }
    public int getMaxCoord() { return maxCoord; }

    public String getFitnessType() { return fitnessType; }

    public String getSelectionType() { return selectionType; }
    public int getTournamentSize() { return tournamentSize; }

    public String getCrossoverType() { return crossoverType; }
    public double getCrossoverRate() { return crossoverRate; }

    public String getMutationType() { return mutationType; }
    public double getMutationRate() { return mutationRate; }

    public String getReplacementType() { return replacementType; }
    public int getEliteCount() { return eliteCount; }

    public int getMaxGenerations() { return maxGenerations; }
    public Long getSeed() { return seed; }

    public boolean isApplyTarget() { return applyTarget; }
    public double getTargetFitness() { return targetFitness; }

    public boolean isApplyConvergence() { return applyConvergence; }
    public double getConvergenceTolerance() { return convergenceTolerance; }
    public int getConvergencePatience() { return convergencePatience; }

    public boolean isApplyDiversity() { return applyDiversity; }
    public double getDiversityThreshold() { return diversityThreshold; }
    public int getDiversityPatience() { return diversityPatience; }

    // ==========================
    // SETTERS (UNCHANGED)
    // ==========================
    public void setTables(Map<String, List<List<String>>> tables) { this.tables = tables; }

    public void setProblemType(String problemType) { this.problemType = problemType; }
    public void setPopulationSize(int populationSize) { this.populationSize = populationSize; }
    public void setGeneLength(int geneLength) { this.geneLength = geneLength; }
    public void setGeneType(String geneType) { this.geneType = geneType; }

    public void setMinGene(int minGene) { this.minGene = minGene; }
    public void setMaxGene(int maxGene) { this.maxGene = maxGene; }

    public void setFitnessType(String fitnessType) { this.fitnessType = fitnessType; }
    public void setCustomFormula(String customFormula) { this.customFormula = customFormula; }

    public void setSelectionType(String selectionType) { this.selectionType = selectionType; }
    public void setTournamentSize(int tournamentSize) { this.tournamentSize = tournamentSize; }

    public void setCrossoverType(String crossoverType) { this.crossoverType = crossoverType; }
    public void setCrossoverRate(double crossoverRate) { this.crossoverRate = crossoverRate; }
    public void setCrossoverIndices(String crossoverIndices) { this.crossoverIndices = crossoverIndices; }

    public void setMutationType(String mutationType) { this.mutationType = mutationType; }
    public void setMutationRate(double mutationRate) { this.mutationRate = mutationRate; }
    public void setMutationIndices(String mutationIndices) { this.mutationIndices = mutationIndices; }

    public void setReplacementType(String replacementType) { this.replacementType = replacementType; }
    public void setEliteCount(int eliteCount) { this.eliteCount = eliteCount; }

    public void setMaxGenerations(int maxGenerations) { this.maxGenerations = maxGenerations; }

    public void setSeed(Long seed) { this.seed = seed; }

    public void setApplyTarget(boolean applyTarget) { this.applyTarget = applyTarget; }
    public void setTargetFitness(double targetFitness) { this.targetFitness = targetFitness; }

    public void setApplyConvergence(boolean applyConvergence) { this.applyConvergence = applyConvergence; }
    public void setConvergenceTolerance(double convergenceTolerance) { this.convergenceTolerance = convergenceTolerance; }
    public void setConvergencePatience(int convergencePatience) { this.convergencePatience = convergencePatience; }

    public void setApplyDiversity(boolean applyDiversity) { this.applyDiversity = applyDiversity; }
    public void setDiversityThreshold(double diversityThreshold) { this.diversityThreshold = diversityThreshold; }
    public void setDiversityPatience(int diversityPatience) { this.diversityPatience = diversityPatience; }
}