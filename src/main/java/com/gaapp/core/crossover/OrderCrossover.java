package com.gaapp.core.crossover;

import com.gaapp.core.model.Gene;
import com.gaapp.core.model.Individual;
import com.gaapp.core.util.RandomUtil;

import java.util.*;

public class OrderCrossover implements CrossoverStrategy {

    @Override
    public List<Individual> crossover(Individual p1, Individual p2, double crossoverRate, int... indices) {

        // ==========================
        // NO CROSSOVER CASE
        // ==========================
        if (RandomUtil.nextDouble() > crossoverRate) {

            Individual c1 = p1.copy();
            Individual c2 = p2.copy();

            c1.setCrossover(p1, p2, "No Crossover");
            c2.setCrossover(p1, p2, "No Crossover");

            return List.of(c1, c2);
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
        // SELECT CUT POINTS
        // ==========================
        int start, end;

        if (indices.length < 2) {
            start = RandomUtil.nextInt(size);
            end = RandomUtil.nextInt(size);

            while (start == end) { // ensure distinct
                end = RandomUtil.nextInt(size);
            }
        } else {
            start = indices[0];
            end = indices[1];

            if (start < 0 || start >= size || end < 0 || end >= size) {
                throw new IllegalArgumentException("Crossover points must be between 0 and " + (size - 1));
            }

            if (start == end) {
                throw new IllegalArgumentException("Crossover points must be different");
            }
        }

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        // ==========================
        // CREATE CHILDREN
        // ==========================
        List<Gene> child1Genes = createChild(p1, p2, start, end);
        List<Gene> child2Genes = createChild(p2, p1, start, end);

        Individual c1 = new Individual(child1Genes, "O1");
        Individual c2 = new Individual(child2Genes, "O2");

        // ==========================
        // SET METADATA (CRITICAL)
        // ==========================
        String action = "Order Crossover between " + start + " and " + end;

        c1.setCrossover(p1, p2, action);
        c2.setCrossover(p1, p2, action);

        return List.of(c1, c2);
    }

    private List<Gene> createChild(Individual parent1, Individual parent2, int start, int end) {

        int size = parent1.getGenes().size();

        List<Gene> child = new ArrayList<>(Collections.nCopies(size, null));

        Set<Object> used = new HashSet<>();

        // ==========================
        // COPY SEGMENT
        // ==========================
        for (int i = start; i <= end; i++) {
            Gene g = parent1.getGenes().get(i).copy();
            child.set(i, g);
            used.add(g.getValue());
        }

        // ==========================
        // FILL REMAINING FROM P2
        // ==========================
        int currentIndex = (end + 1) % size;

        for (int i = 0; i < size; i++) {

            Gene g = parent2.getGenes().get((end + 1 + i) % size);

            if (!used.contains(g.getValue())) {

                child.set(currentIndex, g.copy());
                used.add(g.getValue());

                currentIndex = (currentIndex + 1) % size;
            }
        }

        // ==========================
        // SAFETY CHECK (VERY IMPORTANT)
        // ==========================
        for (int i = 0; i < size; i++) {
            if (child.get(i) == null) {
                throw new IllegalStateException("Order Crossover failed: null gene at index " + i);
            }
        }

        return child;
    }
}