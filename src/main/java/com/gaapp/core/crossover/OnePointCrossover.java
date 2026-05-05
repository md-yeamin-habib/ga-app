package com.gaapp.core.crossover;

import com.gaapp.core.model.Gene;
import com.gaapp.core.model.Individual;
import com.gaapp.core.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;

public class OnePointCrossover implements CrossoverStrategy {

    @Override
    public List<Individual> crossover(Individual p1, Individual p2, double crossoverRate, int... indices) {

        List<Individual> result = new ArrayList<>();

        // ==========================
        // NO CROSSOVER
        // ==========================
        if (RandomUtil.nextDouble() > crossoverRate) {

            Individual c1 = p1.copy();
            Individual c2 = p2.copy();

            // Still record action for UI
            c1.setCrossover(p1, p2, "No crossover");
            c2.setCrossover(p1, p2, "No crossover");

            result.add(c1);
            result.add(c2);
            return result;
        }

        // ==========================
        // VALIDATION
        // ==========================
        if (p1.getGenes().size() != p2.getGenes().size()) {
            throw new IllegalArgumentException("Parents must have same gene length");
        }

        int size = p1.getGenes().size();

        if (size < 2) {
            throw new IllegalArgumentException("Gene length must be at least 2 for crossover");
        }

        // ==========================
        // CROSSOVER POINT
        // ==========================
        int point;

        if (indices.length < 1) {
            point = RandomUtil.nextInt(1, size);
        } else {
            point = indices[0];
            if (point < 1 || point >= size) {
                throw new IllegalArgumentException(
                        "Crossover point must be between 1 and " + (size - 1)
                );
            }
        }

        // ==========================
        // BUILD CHILDREN
        // ==========================
        List<Gene> child1Genes = new ArrayList<>();
        List<Gene> child2Genes = new ArrayList<>();

        for (int i = 0; i < size; i++) {

            if (i < point) {
                child1Genes.add(p1.getGenes().get(i).copy());
                child2Genes.add(p2.getGenes().get(i).copy());
            } else {
                child1Genes.add(p2.getGenes().get(i).copy());
                child2Genes.add(p1.getGenes().get(i).copy());
            }
        }

        // ==========================
        // CREATE CHILDREN
        // ==========================
        Individual child1 = new Individual(child1Genes, "O1");
        Individual child2 = new Individual(child2Genes, "O2");

        // ==========================
        // SET PARENT + ACTION
        // ==========================
        String action = "One Point at index " + point;

        child1.setCrossover(p1, p2, action);
        child2.setCrossover(p1, p2, action);

        result.add(child1);
        result.add(child2);

        return result;
    }
}