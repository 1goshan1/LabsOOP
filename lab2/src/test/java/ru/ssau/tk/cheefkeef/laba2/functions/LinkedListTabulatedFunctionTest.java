package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionTest {

    @Test
    void testConstructorFromArrays() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        assertEquals(3, func.getCount());
        assertEquals(1.0, func.leftBound());
        assertEquals(3.0, func.rightBound());
        assertEquals(4.0, func.getY(1));
        assertEquals(1, func.indexOfX(2.0));
        assertEquals(-1, func.indexOfX(5.0));
    }

    @Test
    void testConstructorFromFunction() {
        MathFunction sqr = new SqrFunction();
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(sqr, 0.0, 2.0, 3);

        assertEquals(3, func.getCount());
        assertEquals(0.0, func.getX(0));
        assertEquals(1.0, func.getX(1));
        assertEquals(2.0, func.getX(2));
        assertEquals(0.0, func.getY(0));
        assertEquals(1.0, func.getY(1));
        assertEquals(4.0, func.getY(2));
    }

    @Test
    void testSinglePoint() {
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(new double[]{5.0}, new double[]{25.0});
        assertEquals(1, func.getCount());
        assertEquals(5.0, func.leftBound());
        assertEquals(5.0, func.rightBound());
        assertEquals(25.0, func.apply(100.0)); // экстраполяция → возвращает 25
        assertEquals(25.0, func.apply(0.0));
    }

    @Test
    void testSetY() {
        double[] x = {0.0, 1.0};
        double[] y = {0.0, 1.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);
        func.setY(1, 10.0);
        assertEquals(10.0, func.getY(1));
    }

    @Test
    void testFloorIndexOfX() {
        double[] x = {0.0, 1.0, 2.0, 3.0};
        double[] y = {0.0, 1.0, 4.0, 9.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        assertEquals(0, func.floorIndexOfX(0.5));
        assertEquals(1, func.floorIndexOfX(1.9));
        assertEquals(3, func.floorIndexOfX(3.0));
        assertEquals(3, func.floorIndexOfX(10.0));
        assertEquals(-1, func.floorIndexOfX(-1.0));
    }

    @Test
    void testInterpolation() {
        double[] x = {0.0, 2.0};
        double[] y = {0.0, 4.0}; // y = x^2
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        assertEquals(2.0, func.apply(1.0), 1e-10); // y = 0 + (4-0)*(1-0)/(2-0) = 2.0
        assertEquals(2.0, func.apply(1.0), 1e-10);
    }

    @Test
    void testExtrapolationLeft() {
        double[] x = {1.0, 2.0};
        double[] y = {1.0, 4.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);
        // Линейная экстраполяция влево: y = 1 + (4-1)*(x-1)/(1) = 1 + 3*(x-1)
        // При x=0: y = 1 - 3 = -2
        assertEquals(-2.0, func.apply(0.0), 1e-10);
    }

    @Test
    void testExtrapolationRight() {
        double[] x = {1.0, 2.0};
        double[] y = {1.0, 4.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);
        // При x=3: y = 1 + 3*(3-1) = 7
        assertEquals(7.0, func.apply(3.0), 1e-10);
    }

    @Test
    void testXFromGreaterThanXTo() {
        MathFunction id = new IdentityFunction();
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(id, 2.0, 0.0, 3);
        // Должно стать [0.0, 1.0, 2.0]
        assertEquals(0.0, func.getX(0));
        assertEquals(1.0, func.getX(1));
        assertEquals(2.0, func.getX(2));
    }

    @Test
    void testSameXFromXTo() {
        MathFunction constFunc = new ConstantFunction(5.0);
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(constFunc, 3.0, 3.0, 4);
        for (int i = 0; i < 4; i++) {
            assertEquals(3.0, func.getX(i));
            assertEquals(5.0, func.getY(i));
        }
    }

    @Test
    void testInsertAtBeginning() {
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0}
        );
        func.insert(0.5, 0.25);
        assertEquals(0.5, func.getX(0));
        assertEquals(0.25, func.getY(0));
        assertEquals(4, func.getCount());
        assertEquals(0.5, func.leftBound());
    }

    @Test
    void testInsertAtEnd() {
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0}
        );
        func.insert(3.0, 9.0);
        assertEquals(3.0, func.getX(2));
        assertEquals(9.0, func.getY(2));
        assertEquals(3.0, func.rightBound());
    }

    @Test
    void testInsertExistingX() {
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0}
        );
        func.insert(2.0, 5.0);
        assertEquals(5.0, func.getY(1));
        assertEquals(3, func.getCount()); // без увеличения
    }
}