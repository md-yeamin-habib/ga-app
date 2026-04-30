package com.gaapp.core.mutation;

import com.gaapp.core.model.Gene;
import com.gaapp.core.model.Individual;
import com.gaapp.core.util.RandomUtil;

import java.util.Collections;
import java.util.List;

public class InversionMutation implements MutationStrategy {

    @Override
    public void mutate(Individual individual, double mutationRate, int... indices) {

        if (RandomUtil.nextDouble() >= mutationRate) {
            individual.setMutation(individual.copyGenes(), "No Mutation");
            return;
        }

        int start, end;
        if (indices.length < 2) {
            start = RandomUtil.nextInt(0, individual.getGenes().size() - 1);
            end = RandomUtil.nextInt(0, individual.getGenes().size() - 1);
        } else {
            start = indices[0];
            end = indices[1];
        }

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        List<Gene> genes = individual.getGenes();

        if (start < 0 || end >= genes.size()) return;

        individual.setMutation(individual.copyGenes(), "Genes between " + start + " and " + end + " inverted");

        List<Gene> subList = genes.subList(start, end + 1);
        Collections.reverse(subList);
    }
}