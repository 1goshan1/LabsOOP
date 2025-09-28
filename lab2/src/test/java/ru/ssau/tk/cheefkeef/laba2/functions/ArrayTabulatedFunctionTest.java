package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
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
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{0}, new double[]{0});
        assertEquals(1, f.getCount());
    }

    @Test
    void getX() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{10.0}, new double[]{100.0});
        assertEquals(10.0, f.getX(0));
    }

    @Test
    void getY() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0}, new double[]{1.0});
        assertEquals(1.0, f.getY(0));
    }

    @Test
    void setY() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0}, new double[]{1.0});
        f.setY(0, 5.0);
        assertEquals(5.0, f.getY(0));
    }

    @Test
    void indexOfX() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        assertEquals(1, f.indexOfX(2.0));
    }

    @Test
    void indexOfY() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        assertEquals(1, f.indexOfY(4.0));
    }

    @Test
    void leftBound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{5.0, 6.0}, new double[]{25.0, 36.0});
        assertEquals(5.0, f.leftBound());
    }

    @Test
    void rightBound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{5.0, 6.0}, new double[]{25.0, 36.0});
        assertEquals(6.0, f.rightBound());
    }

    @Test
    void apply_interpolated() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{0.0, 2.0}, new double[]{0.0, 4.0});
        assertEquals(2.0, f.apply(1.0), 1e-10); // линейная интерполяция
    }

    @Test
    void remove_middle() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});
        f.remove(1);
        assertEquals(2, f.getCount());
        assertEquals(3.0, f.getX(1));
    }
}