package com.gaapp.core.problem;

import java.util.ArrayList;
import java.util.List;

import com.gaapp.core.problem.Point;
import com.gaapp.core.problem.ProblemData;
import com.gaapp.core.problem.ProblemType;

public class TSPProblem implements ProblemData {

    private final List<Point> points;
    private final double[][] distanceMatrix;

    public TSPProblem(List<Point> points, double[][] matrix) {

        if (points.size() != matrix.length ||
            matrix.length != matrix[0].length) {
            throw new IllegalArgumentException("Points and distance matrix size mismatch");
        }

        if (points.size() < 2) {
            throw new IllegalArgumentException("At least 2 points required for TSP");
        }

        if (points == null || matrix == null) {
            throw new IllegalArgumentException("Points and distance matrix cannot be null");
        }

        this.points = new ArrayList<>(points); // defensive copy
        this.distanceMatrix = matrix;
    }

    @Override
    public ProblemType getType() {
        return ProblemType.TSP;
    }

    public int size() {
        return points.size();
    }

    public double distance(int i, int j) {
        return distanceMatrix[i][j];
    }

    public List<Point> getPoints() {
        return new ArrayList<>(points);
    }

    public double[][] getDistanceMatrix() {
        return distanceMatrix.clone();
    }

    @Override
    public void printDebug() {

        System.out.println("\n=== TSP PROBLEM ===");
        System.out.println("Cities: " + points.size());

        System.out.println("\nIndex | X | Y");
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            System.out.printf("%5d | %6.2f | %6.2f%n", i, p.getX(), p.getY());
        }

        System.out.println("\nDistance Matrix:");
        for (int i = 0; i < distanceMatrix.length; i++) {
            for (int j = 0; j < distanceMatrix[i].length; j++) {
                System.out.printf("%8.2f ", distanceMatrix[i][j]);
            }
            System.out.println();
        }
    }
}