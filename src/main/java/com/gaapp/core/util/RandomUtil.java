package com.gaapp.core.util;

import java.util.Random;

public class RandomUtil {

    private static Random RANDOM = new Random();
    private static Long currentSeed = null;

    // =========================
    // SEED HANDLING
    // =========================

    public static void setSeed(long seed) {
        currentSeed = seed;
        RANDOM = new Random(seed);
    }

    public static long setRandomSeed() {
        long seed = new Random().nextLong();
        setSeed(seed);
        return seed;
    }

    public static Long getCurrentSeed() {
        return currentSeed;
    }

    // =========================
    // RANDOM FUNCTIONS
    // =========================

    public static int nextInt(int bound) {
        return RANDOM.nextInt(bound);
    }

    public static int nextInt(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }

    public static double nextDouble() {
        return round(RANDOM.nextDouble(), 5);
    }

    public static double nextDouble(double min, double max) {
        double value = min + (max - min) * RANDOM.nextDouble();
        return round(value, 5);
    }

    // =========================
    // UTILITY
    // =========================

    public static double round(double number, int places) {
        double scale = Math.pow(10, places);
        return Math.round(number * scale) / scale;
    }
}