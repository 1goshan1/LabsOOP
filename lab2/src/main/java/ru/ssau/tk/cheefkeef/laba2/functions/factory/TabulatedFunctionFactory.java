package ru.ssau.tk.cheefkeef.laba2.functions.factory;

import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;

public interface TabulatedFunctionFactory {
    TabulatedFunction create(double[] xValues, double[] yValues);
}