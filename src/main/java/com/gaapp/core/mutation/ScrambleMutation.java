package com.gaapp.core.mutation;

import com.gaapp.core.model.Gene;
import com.gaapp.core.model.Individual;
import com.gaapp.core.util.RandomUtil;

import java.util.*;

public class ScrambleMutation implements MutationStrategy {

    @Override
    public void mutate(Individual individual, double mutationRate, int... indices) {

        // ==========================
        // NO MUTATION CASE
        // ==========================
        if (RandomUtil.nextDouble() >= mutationRate) {
            individual.setMutation(individual.copyGenes(), "No Mutation");
            return;
        }

        int size = individual.getGenes().size();

        int start, end;

        if (indices.length < 2) {
            start = RandomUtil.nextInt(0, size - 1);
            end = RandomUtil.nextInt(0, size - 1);

            while (start == end) { 
                end = RandomUtil.nextInt(0, size - 1);
            }

        } else {
            start = indices[0];
            end = indices[1];
        }

        // ==========================
        // VALIDATION
        // ==========================
        if (start < 0 || end >= size) {
            throw new IllegalArgumentException("Mutation indices out of bounds");
        }

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        List<Gene> genes = individual.getGenes();

        individual.setMutation(
                individual.copyGenes(),
                "Genes between indices " + start + " and " + end + " scrambled"
        );

        // ==========================
        // APPLY SCRAMBLE
        // ==========================
        List<Gene> subList = genes.subList(start, end + 1);
        Collections.shuffle(subList);
    }
}