package com.gaapp.core.util;

import java.util.List;
import com.gaapp.core.problem.Point;

public class DistanceUtil {

    public static double[][] buildMatrix(List<Point> points) {

        int n = points.size();
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            double x1 = points.get(i).getX();
            double y1 = points.get(i).getY();

            for (int j = 0; j < n; j++) {
                double x2 = points.get(j).getX();
                double y2 = points.get(j).getY();

                double dx = x1 - x2;
                double dy = y1 - y2;

                matrix[i][j] = Math.sqrt(dx * dx + dy * dy);
            }
        }

        return matrix;
    }
}