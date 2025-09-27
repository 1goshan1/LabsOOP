package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZeroFunctionTest {
    @Test
    void testFixedValue() {
        MathFunction cf = new ZeroFunction();
        assertEquals(0.0, cf.apply(2.0), 1e-10);
    }

    @Test
    void testSameValue() {
        MathFunction cf = new ZeroFunction();
        assertEquals(0.0, cf.apply(3), 1e-10);
    }
}
