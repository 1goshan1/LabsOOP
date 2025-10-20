package ru.ssau.tk.cheefkeef.laba2.operations;

import ru.ssau.tk.cheefkeef.laba2.concurrent.SynchronizedTabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.TabulatedFunctionFactory;
import ru.ssau.tk.cheefkeef.laba2.functions.Point;

public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {

    private TabulatedFunctionFactory factory;

    public TabulatedDifferentialOperator() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    @Override
    public TabulatedFunction derive(TabulatedFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("Function must not be null");
        }

        Point[] points = TabulatedFunctionOperationService.asPoints(function);
        int n = points.length;

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        // Копируем узлы сетки — они остаются теми же
        for (int i = 0; i < n; i++) {
            xValues[i] = points[i].x;
        }

        // Численное дифференцирование:
        // Для первой точки используем правую разностную производную:
        // f'(x0) =примерно (f(x1) - f(x0)) / (x1 - x0)
        yValues[0] = (points[1].y - points[0].y) / (points[1].x - points[0].x);

        // Для последней точки используем левую разностную производную:
        // f'(xn-1) =примерно (f(xn-1) - f(xn-2) / (xn-1 - xn-2)
        yValues[n - 1] = (points[n - 1].y - points[n - 2].y) / (points[n - 1].x - points[n - 2].x);

        // Для внутренних точек используем центральную разностную производную:
        // f'(xi) =примерно (f(xi-1) - f(xi-1)) / (xi+1 - xi-1),   где 1 <= i <= n-2
        for (int i = 1; i < n - 1; i++) {
            yValues[i] = (points[i + 1].y - points[i - 1].y) / (points[i + 1].x - points[i - 1].x);
        }

        return factory.create(xValues, yValues);
    }

    public TabulatedFunction deriveSynchronously(TabulatedFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("Function must not be null");
        }

        // Если уже синхронизированная — используем как есть
        SynchronizedTabulatedFunction syncFunction;
        if (function instanceof SynchronizedTabulatedFunction) {
            syncFunction = (SynchronizedTabulatedFunction) function;
        } else {
            syncFunction = new SynchronizedTabulatedFunction(function);
        }

        // Выполняем derive() внутри единого блока синхронизации
        return syncFunction.doSynchronously(this::derive);
    }
}