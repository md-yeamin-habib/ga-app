package com.gaapp.core.mutation;

import com.gaapp.core.model.BinaryGene;
import com.gaapp.core.model.Gene;
import com.gaapp.core.model.Individual;
import com.gaapp.core.util.RandomUtil;

public class BitFlipMutation implements MutationStrategy {

    @Override
    public void mutate(Individual individual, double mutationRate, int... indices) {

        if (RandomUtil.nextDouble() >= mutationRate) {
            individual.setMutation(individual.copyGenes(), "No Mutation");
            return;
        }

        int idx;
        if (indices.length < 1) {
            idx = RandomUtil.nextInt(0, individual.getGenes().size() - 1);
        } else {
            idx = indices[0];
        }

        if (idx < 0 || idx >= individual.getGenes().size()) return;

        individual.setMutation(individual.copyGenes(), "Bit flipped at index " + idx);

        Gene g = individual.getGenes().get(idx);

        if (g instanceof BinaryGene) {
            ((BinaryGene) g).flip();
        }
    }
}