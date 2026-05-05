package com.gaapp.core.selection;

import com.gaapp.core.model.Individual;
import com.gaapp.core.model.Population;
import com.gaapp.core.util.RandomUtil;

import com.gaapp.core.selection.DirectPickSelection;

import java.util.ArrayList;
import java.util.List;

public class RouletteSelection implements SelectionStrategy {

    private final SelectionStrategy fallback = new DirectPickSelection();

    @Override
    public List<Individual> select(List<Individual> population, int count) {

        if (population.isEmpty()) {
            throw new IllegalArgumentException("Population is empty");
        }

        int N = population.size();

        if (count > N) {
            throw new IllegalArgumentException("Selection count cannot exceed population size");
        }

        double minFitness = population.stream()
                .mapToDouble(Individual::getFitness)
                .min()
                .orElse(0);

        double shift = (minFitness < 0) ? -minFitness + 1 : 0;

        double totalFitness = population.stream()
                .mapToDouble(ind -> ind.getFitness() + shift)
                .sum();

        double averageFitness = totalFitness / N;

        if (totalFitness <= 1e-12) {
            // fallback: direct pick selection
            population.forEach(ind -> ind.setRouletteSelection(0.0, 0.0, 0.0));
            return fallback.select(population, count);
        }

        List<Individual> selected = new ArrayList<>();

        for (Individual ind : population) {
            double adjustedFitness = ind.getFitness() + shift;
            ind.setRouletteSelection(adjustedFitness, adjustedFitness / totalFitness, adjustedFitness / averageFitness);
        }

        for (int i = 0; i < count; i++) {

            double rand = RandomUtil.nextDouble() * totalFitness;
            double cumulative = 0;

            for (Individual ind : population) {
                cumulative += ind.getAdjustedFitness();
                if (cumulative >= rand) {
                    selected.add(ind.copy());
                    break;
                }
            }
        }

        return selected;
    }
}