package com.gaapp.core.selection;

import com.gaapp.core.model.Individual;
import com.gaapp.core.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;

public class RankSelection implements SelectionStrategy {

    @Override
    public List<Individual> select(List<Individual> population, int count) {

        List<Individual> individuals = new ArrayList<>(population);
        int N = individuals.size();

        if (count > N) {
            throw new IllegalArgumentException("Selection count cannot exceed population size");
        }

        if (individuals.isEmpty()) {
            throw new IllegalArgumentException("Population is empty");
        }

        // ==========================
        // 1. SORT BY FITNESS DESC
        // ==========================
        individuals.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));

        // ==========================
        // 2. ASSIGN RANK + PROBABILITY
        // ==========================
        int n = individuals.size();

        double totalRankWeight = n * (n + 1) / 2.0;

        for (int i = 0; i < n; i++) {
            int rank = i + 1;
            int weight = n - i;
            double probability = weight / totalRankWeight;

            individuals.get(i).setRankSelection(rank, weight, probability);
        }

        // ==========================
        // 3. BUILD CUMULATIVE ARRAY
        // ==========================
        double[] cumulative = new double[n];

        double cumulativeSum = 0;

        for (int i = 0; i < n; i++) {
            cumulativeSum += individuals.get(i).getRankProbability();
            cumulative[i] = cumulativeSum;
        }

        // ==========================
        // 4. SELECTION 
        // ==========================
        List<Individual> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            double r = RandomUtil.nextDouble();

            int selectedIndex = binarySearch(cumulative, r);

            result.add(individuals.get(selectedIndex).copy());
        }

        return result;
    }

    private int binarySearch(double[] cumulative, double value) {

        int low = 0;
        int high = cumulative.length - 1;

        while (low < high) {
            int mid = (low + high) / 2;

            if (value <= cumulative[mid]) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return low;
    }
}