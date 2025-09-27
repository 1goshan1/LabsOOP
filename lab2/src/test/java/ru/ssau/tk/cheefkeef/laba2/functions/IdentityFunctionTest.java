package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IdentityFunctionTest {

    @Test
    void apply() {
        MathFunction id = new IdentityFunction();

        assertEquals(0, id.apply(0), 0.0001);
        assertEquals(5, id.apply(5), 0.0001);
        assertEquals(100.5, id.apply(100.5), 0.0001);
        assertEquals(-3, id.apply(-3), 0.0001);
        assertEquals(-0.001, id.apply(-0.001), 0.0001);
        assertEquals(Double.MAX_VALUE, id.apply(Double.MAX_VALUE), 0.0001);
        assertEquals(Double.MIN_VALUE, id.apply(Double.MIN_VALUE), 0.0001);
    }
}