package ru.ssau.tk.cheefkeef.laba2.operations;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.cheefkeef.laba2.functions.MathFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.SqrFunction;

import static org.junit.jupiter.api.Assertions.*;

public class SteppingDifferentialOperatorTest {

    @Test
    public void testLeftDerivative() {
        double step = 0.001;
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(step);
        MathFunction sqr = new SqrFunction();
        MathFunction derivative = operator.derive(sqr);

        // f'(x) = 2x
        assertEquals(0.0, derivative.apply(0.0), 1e-2);
        assertEquals(2.0, derivative.apply(1.0), 1e-2);
        assertEquals(-2.0, derivative.apply(-1.0), 1e-2);
    }

    @Test
    public void testRightDerivative() {
        double step = 0.001;
        RightSteppingDifferentialOperator operator = new RightSteppingDifferentialOperator(step);
        MathFunction sqr = new SqrFunction();
        MathFunction derivative = operator.derive(sqr);

        assertEquals(0.0, derivative.apply(0.0), 1e-2);
        assertEquals(2.0, derivative.apply(1.0), 1e-2);
        assertEquals(-2.0, derivative.apply(-1.0), 1e-2);
    }

    @Test
    public void testInvalidStep() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LeftSteppingDifferentialOperator(0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new LeftSteppingDifferentialOperator(-0.1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new LeftSteppingDifferentialOperator(Double.NaN);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new LeftSteppingDifferentialOperator(Double.POSITIVE_INFINITY);
        });
    }

    @Test
    public void testStepSetter() {
        LeftSteppingDifferentialOperator op = new LeftSteppingDifferentialOperator(0.1);
        assertEquals(0.1, op.getStep(), 1e-2);

        op.setStep(0.01);
        assertEquals(0.01, op.getStep(), 1e-2);

        assertThrows(IllegalArgumentException.class, () -> {
            op.setStep(-0.01);
        });
    }
}