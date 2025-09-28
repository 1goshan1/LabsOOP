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
        assertEquals(6.25, h.apply(1.5), 1e-10);
    }

    @Test
    void linkedListTabulatedAndIdentity() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);
        IdentityFunction id = new IdentityFunction();
        MathFunction h = f.andThen(id);

        assertEquals(20.0, h.apply(2.0), 1e-10);
        assertEquals(15.0, h.apply(1.5), 1e-10);
    }

    @Test
    void arrayTabulatedAndLinkedListTabulated() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new SqrFunction(), 0.0, 2.0, 3);
        LinkedListTabulatedFunction g = new LinkedListTabulatedFunction(
                x -> x * 2,
                0.0, 4.0, 3
        );
        MathFunction h = f.andThen(g);

        assertEquals(0.0, h.apply(0.0), 1e-10);
        assertEquals(2.0, h.apply(1.0), 1e-10);
        assertEquals(8.0, h.apply(2.0), 1e-10);
        assertEquals(5.0, h.apply(1.5), 1e-10);
    }
}