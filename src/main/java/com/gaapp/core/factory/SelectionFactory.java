package com.gaapp.core.factory;

import com.gaapp.core.selection.*;

public class SelectionFactory {

    public enum SelectionType {
        ROULETTE,
        RANK,
        TOURNAMENT,
        DIRECT_PICK
    }

    public static class SelectionConfig {

        private final SelectionStrategy strategy;

        public SelectionConfig(SelectionStrategy strategy) {
            this.strategy = strategy;
        }

        public SelectionStrategy getStrategy() {
            return strategy;
        }
    }

    public static SelectionConfig create(SelectionType type, int... args) {

        return switch (type) {

            case ROULETTE -> new SelectionConfig(
                    new RouletteSelection()
            );

            case RANK -> new SelectionConfig(
                    new RankSelection()
            );

            case DIRECT_PICK -> new SelectionConfig(
                    new DirectPickSelection()
            );

            case TOURNAMENT -> {

                int size = 3;

                if (args.length > 0) {
                    size = args[0];
                }

                yield new SelectionConfig(
                        new TournamentSelection(size)
                );
            }
        };
    }
}