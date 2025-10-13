package ru.ssau.tk.cheefkeef.laba2.operations;

import ru.ssau.tk.cheefkeef.laba2.exceptions.InconsistentFunctionsException;
import ru.ssau.tk.cheefkeef.laba2.functions.Point;
import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.TabulatedFunctionFactory;

public class TabulatedFunctionOperationService {
    @FunctionalInterface
    private interface BiOperation {
        double apply(double u, double v);
    }

    private TabulatedFunctionFactory factory;

    public TabulatedFunctionOperationService() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    private TabulatedFunction doOperation(TabulatedFunction a, TabulatedFunction b, BiOperation operation) {
        if (a == null || b == null) {
            throw new NullPointerException("Functions must not be null");
        }

        int countA = a.getCount();
        int countB = b.getCount();

        if (countA != countB) {
            throw new InconsistentFunctionsException("Functions have different number of points");
        }

        Point[] pointsA = asPoints(a);
        Point[] pointsB = asPoints(b);

        double[] xValues = new double[countA];
        double[] yValues = new double[countA];

        for (int i = 0; i < countA; i++) {
            double xA = pointsA[i].x;
            double xB = pointsB[i].x;

            if (xA != xB) {
                throw new InconsistentFunctionsException(
                        String.format("X-values differ at index %d: %f vs %f", i, xA, xB)
                );
            }

            xValues[i] = xA;
            yValues[i] = operation.apply(pointsA[i].y, pointsB[i].y);
        }

        return factory.create(xValues, yValues);
    }

    public TabulatedFunction add(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u + v);
    }

    public TabulatedFunction subtract(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u - v);
    }

    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        int i = 0;
        Point[] points = new Point[tabulatedFunction.getCount()];
        for (Point point : tabulatedFunction) {
            points[i] = point;
            i++;
        }

        return points;
    }
}
