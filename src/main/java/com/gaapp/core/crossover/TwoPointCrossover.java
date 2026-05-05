package com.gaapp.core.crossover;

import com.gaapp.core.model.Gene;
import com.gaapp.core.model.Individual;
import com.gaapp.core.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;

public class TwoPointCrossover implements CrossoverStrategy {

    @Override
    public List<Individual> crossover(Individual p1, Individual p2, double crossoverRate, int... indices) {

        List<Individual> result = new ArrayList<>();

        // ==========================
        // NO CROSSOVER
        // ==========================
        if (RandomUtil.nextDouble() > crossoverRate) {

            Individual c1 = p1.copy();
            Individual c2 = p2.copy();

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

        if (size < 3) {
            throw new IllegalArgumentException("Gene length must be at least 3 for two-point crossover");
        }

        // ==========================
        // CROSSOVER POINTS
        // ==========================
        int point1, point2;

        if (indices.length < 2) {

            // ensure valid range: 1 ≤ p1 < p2 < size
            point1 = RandomUtil.nextInt(1, size - 1);
            point2 = RandomUtil.nextInt(1, size - 1);

            while (point1 == point2) {
                point2 = RandomUtil.nextInt(1, size - 1);
            }

        } else {

            point1 = indices[0];
            point2 = indices[1];

            if (point1 < 1 || point2 < 1 || point1 >= size - 1 || point2 >= size - 1) {
                throw new IllegalArgumentException(
                        "Crossover points must be between 1 and " + (size - 2)
                );
            }

            if (point1 == point2) {
                throw new IllegalArgumentException("Crossover points must be different");
            }
        }

        // ensure order
        if (point1 > point2) {
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }

        // ==========================
        // BUILD CHILDREN
        // ==========================
        List<Gene> child1Genes = new ArrayList<>();
        List<Gene> child2Genes = new ArrayList<>();

        for (int i = 0; i < size; i++) {

            if (i < point1 || i >= point2) {
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
        String action = "Two Point between " + point1 + " and " + point2;

        child1.setCrossover(p1, p2, action);
        child2.setCrossover(p1, p2, action);

        result.add(child1);
        result.add(child2);

        return result;
    }
}