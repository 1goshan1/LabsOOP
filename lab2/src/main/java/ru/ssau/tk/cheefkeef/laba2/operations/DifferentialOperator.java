package ru.ssau.tk.cheefkeef.laba2.operations;

import ru.ssau.tk.cheefkeef.laba2.functions.MathFunction;

public interface DifferentialOperator<T extends MathFunction> {
    T derive(T function);
}