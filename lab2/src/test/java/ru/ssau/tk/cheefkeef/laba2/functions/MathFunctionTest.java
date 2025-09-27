package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MathFunctionTest {
    @Test
    void testCompositeId() {
        MathFunction f = new SqrFunction();
        MathFunction g = new IdentityFunction();
        MathFunction h = f.andThen(g);
        assertEquals(4.0, h.apply(2.0));
    }

    @Test
    void testCompositeSqr() {
        MathFunction f = new SqrFunction();
        MathFunction g = new SqrFunction();
        MathFunction h = new SqrFunction();
        MathFunction e = f.andThen(g).andThen(h); // f(g(h(x))) -> f(g(h(2))) -> f(g(4)) -> f(16) -> 256
        assertEquals(256, e.apply(2.0));
    }
}
