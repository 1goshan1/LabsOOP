package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimpleIterativeMethodTest {

    @Test
    void testConvergesToFixedPoint_SqrtExample() {
        MathFunction phi = x -> Math.sqrt(x + 2); // I've used lambda functions somewhere due to lack of functional classes
        SimpleIterativeMethod method = new SimpleIterativeMethod(phi, 1e-6, 100);
        double result = method.apply(0.0);
        assertEquals(2.0, result, 1e-5);
        assertTrue(Math.abs(phi.apply(result) - result) < 1e-5);
    }

    @Test
    void testIdentityFunction_ReturnsInitialGuessImmediately() {
        MathFunction identity = new IdentityFunction();
        SimpleIterativeMethod method = new SimpleIterativeMethod(identity, 1e-8, 10);
        double initial = 3.14;
        double result = method.apply(initial);
        assertEquals(initial, result, 0.0);
    }

    @Test
    void testConstantFunction_ConvergesInOneIteration() {
        MathFunction constant = new ConstantFunction(5.0);
        SimpleIterativeMethod method = new SimpleIterativeMethod(constant, 1e-9, 10);
        double result = method.apply(100.0);
        assertEquals(5.0, result, 0.0);
        assertEquals(5.0, constant.apply(result), 0.0);
    }

    @Test
    void testDivergentFunction_ExceedsMaxIterations() {
        MathFunction divergent = x -> x + 1;
        SimpleIterativeMethod method = new SimpleIterativeMethod(divergent, 0.1, 5);
        double result = method.apply(0.0);
        assertEquals(5.0, result, 0.0);
    }

    @Test
    void testCosineFixedPoint_KnownValue() {
        MathFunction cos = x -> Math.cos(x);
        SimpleIterativeMethod method = new SimpleIterativeMethod(cos, 1e-7, 100);

        double result = method.apply(1.0);
        double expected = 0.7390851332151607;

        assertEquals(expected, result, 1e-6);
        assertTrue(Math.abs(cos.apply(result) - result) < 1e-6);
    }

    @Test
    void testZeroFunction() {
        SimpleIterativeMethod method = new SimpleIterativeMethod(new ZeroFunction(), 1e-10, 10);
        double result = method.apply(999.0);
        assertEquals(0.0, result, 0.0);
    }

    @Test
    void testUnitFunction() {
        SimpleIterativeMethod method = new SimpleIterativeMethod(new UnitFunction(), 1e-10, 10);
        double result = method.apply(-10.0);
        assertEquals(1.0, result, 0.0);
    }

    @Test
    void testCompositeFunction_IdentityAfterSquare() {
        MathFunction phi = new CompositeFunction(new SqrFunction(), new IdentityFunction());
        SimpleIterativeMethod method = new SimpleIterativeMethod(phi, 1e-8, 100);
        double result = method.apply(0.5);
        assertEquals(0.0, result, 1e-7);
        double divergentResult = method.apply(1.5);
        assertTrue(divergentResult > 100);
    }

    @Test
    void testToleranceZero_StillWorksButMayNotTerminateEarly() {
        MathFunction phi = new IdentityFunction();
        SimpleIterativeMethod method = new SimpleIterativeMethod(phi, 0.0, 5);
        double result = method.apply(42.0);
        assertEquals(42.0, result, 0.0);
    }
}