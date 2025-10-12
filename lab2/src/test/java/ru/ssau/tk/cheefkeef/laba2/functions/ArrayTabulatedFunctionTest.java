package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.cheefkeef.laba2.exceptions.InterpolationException;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class ArrayTabulatedFunctionTest {

    @Test
    void constructorFromArrays() {
        double[] x = {1.0, 2.0};
        double[] y = {1.0, 4.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);
        assertEquals(2, f.getCount());
    }

    @Test
    void constructorFromFunction() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new SqrFunction(), 0.0, 2.0, 3);
        assertEquals(3, f.getCount());
        assertEquals(4.0, f.getY(2));
    }

    @Test
    void getCount() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{0, 1}, new double[]{0, 1});
        assertEquals(2, f.getCount());
    }

    @Test
    void getX() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{10.0, 42}, new double[]{100.0, 52});
        assertEquals(10.0, f.getX(0));
    }

    @Test
    void getXWithInvalidIndex() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{10.0, 42}, new double[]{100.0, 52});
        assertThrows(IllegalArgumentException.class, () -> f.getX(-1));
        assertThrows(IllegalArgumentException.class, () -> f.getX(2));
    }

    @Test
    void getY() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        assertEquals(1.0, f.getY(0));
        assertEquals(4.0, f.getY(1));
    }

    @Test
    void setY() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        f.setY(0, 5.0);
        assertEquals(5.0, f.getY(0));
        assertEquals(4.0, f.getY(1));
    }

    @Test
    void setYWithInvalidIndex() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        assertThrows(IllegalArgumentException.class, () -> f.setY(-1, 5.0));
        assertThrows(IllegalArgumentException.class, () -> f.setY(2, 5.0));
    }

    @Test
    void indexOfX() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});
        assertEquals(1, f.indexOfX(2.0));
        assertEquals(-1, f.indexOfX(5.0));
    }

    @Test
    void indexOfY() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});
        assertEquals(1, f.indexOfY(4.0));
        assertEquals(-1, f.indexOfY(5.0));
    }

    @Test
    void leftBound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{5.0, 6.0, 7.0}, new double[]{25.0, 36.0, 49.0});
        assertEquals(5.0, f.leftBound());
    }

    @Test
    void rightBound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{5.0, 6.0, 7.0}, new double[]{25.0, 36.0, 49.0});
        assertEquals(7.0, f.rightBound());
    }

    @Test
    void apply_interpolated() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{0.0, 2.0}, new double[]{0.0, 4.0});
        assertEquals(2.0, f.apply(1.0), 1e-10);
    }

    @Test
    void floorIndexOfX() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});
        assertEquals(0, f.floorIndexOfX(1.5));
        assertEquals(1, f.floorIndexOfX(2.0));
        assertEquals(1, f.floorIndexOfX(2.5));
        assertEquals(2, f.floorIndexOfX(3.0));
    }

    @Test
    void floorIndexOfXLessThanLeftBound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});
        assertThrows(IllegalArgumentException.class, () -> f.floorIndexOfX(0.5));
    }

    @Test
    void remove_middle() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});
        f.remove(1);
        assertEquals(2, f.getCount());
        assertEquals(3.0, f.getX(1));
        assertEquals(9.0, f.getY(1));
    }

    @Test
    void removeWithMinimumPoints() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        assertThrows(IllegalStateException.class, () -> f.remove(0));
        assertThrows(IllegalStateException.class, () -> f.remove(1));
    }

    @Test
    void testInsertNewValue() {
        double[] x = {1.0, 2.0, 4.0};
        double[] y = {10.0, 20.0, 40.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);

        func.insert(3.0, 30.0);

        assertEquals(4, func.getCount());
        assertEquals(3.0, func.getX(2));
        assertEquals(30.0, func.getY(2));
    }

    @Test
    void testInsertExistingX() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);

        func.insert(2.0, 25.0);

        assertEquals(3, func.getCount());
        assertEquals(25.0, func.getY(1));
    }

    @Test
    void testInsertAtBeginning() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(
                new double[]{2.0, 3.0}, new double[]{20.0, 30.0}
        );
        func.insert(1.0, 10.0);
        assertEquals(3, func.getCount());
        assertEquals(1.0, func.getX(0));
        assertEquals(10.0, func.getY(0));
        assertEquals(2.0, func.getX(1));
    }

    @Test
    void testInsertAtEnd() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(
                new double[]{1.0, 2.0}, new double[]{10.0, 20.0}
        );
        func.insert(3.0, 30.0);
        assertEquals(3, func.getCount());
        assertEquals(3.0, func.getX(2));
        assertEquals(30.0, func.getY(2));
    }

    @Test
    void testInterpolateXNotEx() {
        double[] x = {1.0, 2.0, 4.0, 5.0, 6.0};
        double[] y = {10.0, 20.0, 40.0, 80.0, 160.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);
        assertThrows(InterpolationException.class, () -> func.interpolate(2.5, 1));
    }

    @Test
    void testInterpolateXEx() {
        double[] x = {1.0, 2.0, 4.0, 5.0, 6.0};
        double[] y = {10.0, 20.0, 40.0, 80.0, 160.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);
        assertThrows(InterpolationException.class, () -> func.interpolate(2.5, 3));
    }
    @Test
    public void testIterator() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(
                new double[]{1.0, 2.0, 3.0, 4.0}, new double[]{10.0, 20.0, 30.0, 40.0}
        );

        assertTrue(func.iterator().hasNext());
        //assertThrows(java.util.NoSuchElementException.class, () -> func.iterator().next());
    }

    @Test
    public void testIteratorEx() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(
                new double[]{1.0, 2.0, 3.0, 4.0}, new double[]{10.0, 20.0, 30.0, 40.0}
        );

        Iterator<Point> it = func.iterator();
        it.next();
        it.next();
        it.next();
        it.next();

        assertFalse(it.hasNext());
        assertThrows(java.util.NoSuchElementException.class, () -> it.next());
    }

    @Test
    public void testIteratorNext() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(
                new double[]{1.0, 2.0, 3.0, 4.0}, new double[]{10.0, 20.0, 30.0, 40.0}
        );

        Iterator<Point> it = func.iterator();
        it.next();

        assertEquals(2.0, it.next().x);
    }
}