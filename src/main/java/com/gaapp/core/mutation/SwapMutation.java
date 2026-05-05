package com.gaapp.core.mutation;

import com.gaapp.core.model.Gene;
import com.gaapp.core.model.Individual;
import com.gaapp.core.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;

public class SwapMutation implements MutationStrategy {

    @Override
    public void mutate(Individual individual, double mutationRate, int... indices) {

        List<Gene> genes = individual.getGenes();
        int size = genes.size();

        // ==========================
        // NO MUTATION CASE
        // ==========================
        if (RandomUtil.nextDouble() >= mutationRate) {
            individual.setMutation(individual.copyGenes(), "No Mutation");
            return;
        }

        int i, j;

        if (indices.length < 2) {
            i = RandomUtil.nextInt(0, size - 1);
            j = RandomUtil.nextInt(0, size - 1);

            while (i == j) { // ensure meaningful swap
                j = RandomUtil.nextInt(0, size - 1);
            }

        } else {
            i = indices[0];
            j = indices[1];
        }

        // ==========================
        // VALIDATION
        // ==========================
        if (i < 0 || j < 0 || i >= size || j >= size) {
            throw new IllegalArgumentException("Swap indices out of bounds");
        }


        individual.setMutation(
                individual.copyGenes(),
                "Genes at indices " + i + " and " + j + " swapped"
        );

        // ==========================
        // APPLY SWAP
        // ==========================
        Gene temp = genes.get(i);
        genes.set(i, genes.get(j));
        genes.set(j, temp);
    }
}