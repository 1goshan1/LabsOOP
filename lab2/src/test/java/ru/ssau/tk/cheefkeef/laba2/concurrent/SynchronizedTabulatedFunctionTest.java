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

    @Test
    public void testDoSynchronouslyWithReturnValue() {
        TabulatedFunction original = new ArrayTabulatedFunction(
                new double[]{1, 2, 3}, new double[]{10, 20, 30}
        );
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(original);

        // найти среднее значение y
        Double average = syncFunc.doSynchronously(func -> {
            double sum = 0;
            for (int i = 0; i < func.getCount(); i++) {
                sum += func.getY(i);
            }
            return sum / func.getCount();
        });

        assertEquals(20.0, average, 1e-10);
    }

    @Test
    public void testDoSynchronouslyWithVoid() {
        TabulatedFunction original = new ArrayTabulatedFunction(
                new double[]{1, 2}, new double[]{10, 20}
        );
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(original);

        //  умножить все y на 2
        syncFunc.doSynchronously(func -> {
            for (int i = 0; i < func.getCount(); i++) {
                func.setY(i, func.getY(i) * 2);
            }
            return null;
        });

        assertEquals(20.0, syncFunc.getY(0), 1e-10);
        assertEquals(40.0, syncFunc.getY(1), 1e-10);
    }

    @Test
    public void testDoSynchronouslyComplexOperation() {
        TabulatedFunction original = new ArrayTabulatedFunction(
                new double[]{0, 1, 2}, new double[]{0, 1, 4}
        );
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(original);

        // вычислить производную в точке и вернуть как строку
        String result = syncFunc.doSynchronously(func -> {
            double x0 = func.getX(1);
            double y0 = func.getY(1);
            double x1 = func.getX(2);
            double y1 = func.getY(2);
            double derivative = (y1 - y0) / (x1 - x0);
            return String.format("f'(%f) = %f", x0, derivative);
        });

        assertEquals("f'(1,000000) = 3,000000", result);
    }
}