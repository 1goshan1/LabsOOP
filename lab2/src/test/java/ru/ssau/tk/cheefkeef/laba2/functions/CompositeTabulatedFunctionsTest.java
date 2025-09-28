package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositeTabulatedFunctionsTest {

    @Test
    void arrayTabulatedAndSqrFunction() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new SqrFunction(), 0.0, 2.0, 3);
        SqrFunction g = new SqrFunction();
        MathFunction h = f.andThen(g);

        assertEquals(1.0, h.apply(1.0), 1e-10);
        assertEquals(16.0, h.apply(2.0), 1e-10);
    }
}