package ru.ssau.tk.cheefkeef.laba2.concurrent;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.cheefkeef.laba2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;

import static org.junit.jupiter.api.Assertions.*;

public class SynchronizedTabulatedFunctionTest {

    @Test
    public void testWrapArrayTabulatedFunction() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        TabulatedFunction original = new ArrayTabulatedFunction(x, y);
        TabulatedFunction wrapped = new SynchronizedTabulatedFunction(original);

        assertEquals(3, wrapped.getCount());
        assertEquals(1.0, wrapped.getX(0), 1e-10);
        assertEquals(20.0, wrapped.getY(1), 1e-10);
        assertEquals(1.0, wrapped.leftBound(), 1e-10);
        assertEquals(3.0, wrapped.rightBound(), 1e-10);
        assertEquals(2, wrapped.indexOfY(30.0));
    }

    @Test
    public void testWrapLinkedListTabulatedFunction() {
        double[] x = {0.5, 1.5, 2.5};
        double[] y = {5.0, 15.0, 25.0};
        TabulatedFunction original = new LinkedListTabulatedFunction(x, y);
        TabulatedFunction wrapped = new SynchronizedTabulatedFunction(original);

        assertEquals(3, wrapped.getCount());
        assertEquals(15.0, wrapped.getY(1), 1e-10);
        assertEquals(0.5, wrapped.getX(0), 1e-10);
        assertEquals(2, wrapped.indexOfX(2.5));
    }

    @Test
    public void testSetY() {
        TabulatedFunction original = new ArrayTabulatedFunction(
                new double[]{1, 2}, new double[]{10, 20}
        );
        TabulatedFunction wrapped = new SynchronizedTabulatedFunction(original);

        wrapped.setY(0, 100.0);
        assertEquals(100.0, wrapped.getY(0), 1e-10);
        assertEquals(100.0, original.getY(0), 1e-10); // делегат изменился
    }

    @Test
    public void testApply() {
        TabulatedFunction original = new ArrayTabulatedFunction(
                new double[]{0, 1, 2}, new double[]{0, 1, 4}
        );
        TabulatedFunction wrapped = new SynchronizedTabulatedFunction(original);

        assertEquals(0.0, wrapped.apply(0.0), 1e-10);
        assertEquals(1.0, wrapped.apply(1.0), 1e-10);
        assertEquals(2.5, wrapped.apply(1.5), 1e-10);
    }

    @Test
    public void testIterator() {
        TabulatedFunction original = new ArrayTabulatedFunction(
                new double[]{1, 2}, new double[]{10, 20}
        );
        TabulatedFunction wrapped = new SynchronizedTabulatedFunction(original);

        var it = wrapped.iterator();
        assertTrue(it.hasNext());
        var p1 = it.next();
        assertEquals(1.0, p1.x, 1e-10);
        assertEquals(10.0, p1.y, 1e-10);

        assertTrue(it.hasNext());
        var p2 = it.next();
        assertEquals(2.0, p2.x, 1e-10);
        assertEquals(20.0, p2.y, 1e-10);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNullDelegate() {
        assertThrows(NullPointerException.class, () -> {
            new SynchronizedTabulatedFunction(null);
        });
    }
}