package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SqrFunctionTest {

    private final MathFunction sqrFunction = new SqrFunction();

    @Test
    void testApply_PositiveNumber() {
        assertEquals(4.0, sqrFunction.apply(2.0), 1e-10);
    }

    @Test
    void testApply_NegativeNumber() {
        assertEquals(9.0, sqrFunction.apply(-3.0), 1e-10);
    }

    @Test
    void testApply_Zero() {
        assertEquals(0.0, sqrFunction.apply(0.0), 1e-10);
    }

    @Test
    void testApply_Fraction() {
        assertEquals(0.25, sqrFunction.apply(0.5), 1e-10);
    }

    @Test
    void testApply_LargeNumber() {
        assertEquals(1e10, sqrFunction.apply(1e5), 1e-5);
    }

    @Test
    void testApply_UsesMathPow() {
        double x = 7.3;
        double expected = Math.pow(x, 2);
        assertEquals(expected, sqrFunction.apply(x), 1e-12);
    }
}