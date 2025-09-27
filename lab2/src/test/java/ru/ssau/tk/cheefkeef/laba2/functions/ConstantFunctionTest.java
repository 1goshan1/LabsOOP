package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConstantFunctionTest {
    @Test
    void testFixedValue() {
        MathFunction cf = new ConstantFunction(52.4);
        assertEquals(52.4, cf.apply(2.0), 1e-10);
    }

    @Test
    void testSameValue() {
        MathFunction cf = new ConstantFunction(3);
        assertEquals(3, cf.apply(3), 1e-10);
    }
}
