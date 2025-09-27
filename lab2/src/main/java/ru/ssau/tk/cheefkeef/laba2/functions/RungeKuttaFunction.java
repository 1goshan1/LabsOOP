package ru.ssau.tk.cheefkeef.laba2.functions;

import java.util.function.BiFunction;

public class RungeKuttaFunction implements MathFunction {

    private final double x0;
    private final double y0;
    private final BiFunction<Double, Double, Double> derivative; // f(x, y) = dy/dx
    private final double step;


    // derivative - функция f(x, y) = dy/dx
    public RungeKuttaFunction(double x0, double y0, BiFunction<Double, Double, Double> derivative, double step) {
        this.x0 = x0;
        this.y0 = y0;
        this.derivative = derivative;
        this.step = Math.abs(step);
    }

    @Override
    public double apply(double x) {
        if (x == x0) return y0;

        double currentX = x0;
        double currentY = y0;
        double h = x > x0 ? step : -step;

        while ((x > x0 && currentX < x) || (x < x0 && currentX > x)) {
            if (Math.abs(x - currentX) < Math.abs(h)) {
                h = x - currentX;
            }

            double k1 = derivative.apply(currentX, currentY);
            double k2 = derivative.apply(currentX + h / 2, currentY + h * k1 / 2);
            double k3 = derivative.apply(currentX + h / 2, currentY + h * k2 / 2);
            double k4 = derivative.apply(currentX + h, currentY + h * k3);

            currentY += h * (k1 + 2 * k2 + 2 * k3 + k4) / 6.0;
            currentX += h;
        }

        return currentY;
    }
}