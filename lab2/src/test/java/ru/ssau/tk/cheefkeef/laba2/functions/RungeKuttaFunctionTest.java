package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
import java.util.function.BiFunction;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RungeKuttaFunctionTest {

    @Test
    void testRk4OnKnownSolution() {
        BiFunction<Double, Double, Double> f = (x, y) -> -2 * x * y * y;
        MathFunction rk = new RungeKuttaFunction(0.0, 1.0, f, 0.001);

        double x = 1.0;
        double expected = 1.0 / (x * x + 1);
        double actual = rk.apply(x);

        assertEquals(expected, actual, 1e-6);
    }

    @Test
    void testAtInitialPoint() {
        BiFunction<Double, Double, Double> f = (x, y) -> x + y;
        MathFunction rk = new RungeKuttaFunction(2.0, 3.0, f, 0.01);
        assertEquals(3.0, rk.apply(2.0), 1e-12);
    }
}