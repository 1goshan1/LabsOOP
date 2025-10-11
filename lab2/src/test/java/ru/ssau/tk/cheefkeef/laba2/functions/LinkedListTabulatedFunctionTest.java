package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.cheefkeef.laba2.exceptions.InterpolationException;

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
    void testConstructorFromArraysThrowsExceptionForSinglePoint() {
        double[] x = {5.0};
        double[] y = {25.0};

        assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunction(x, y));
    }

    @Test
    void testConstructorFromArraysThrowsExceptionForEmptyArrays() {
        double[] x = {};
        double[] y = {};

        assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunction(x, y));
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
    void testConstructorFromFunctionThrowsExceptionForSinglePoint() {
        MathFunction sqr = new SqrFunction();

        assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunction(sqr, 0.0, 2.0, 1));
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
    void testSetYThrowsExceptionForInvalidIndex() {
        double[] x = {0.0, 1.0};
        double[] y = {0.0, 1.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        assertThrows(IndexOutOfBoundsException.class, () -> func.setY(-1, 5.0));
        assertThrows(IndexOutOfBoundsException.class, () -> func.setY(2, 5.0));
    }

    @Test
    void testGetXThrowsExceptionForInvalidIndex() {
        double[] x = {0.0, 1.0};
        double[] y = {0.0, 1.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        assertThrows(IndexOutOfBoundsException.class, () -> func.getX(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> func.getX(2));
    }

    @Test
    void testGetYThrowsExceptionForInvalidIndex() {
        double[] x = {0.0, 1.0};
        double[] y = {0.0, 1.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        assertThrows(IndexOutOfBoundsException.class, () -> func.getY(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> func.getY(2));
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
    }

    @Test
    void testFloorIndexOfXThrowsExceptionForXLessThanLeftBound() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        assertThrows(IllegalArgumentException.class, () -> func.floorIndexOfX(0.5));
    }

    @Test
    void testInterpolation() {
        double[] x = {0.0, 2.0};
        double[] y = {0.0, 4.0}; // y = 2x
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        assertEquals(2.0, func.apply(1.0), 1e-10); // y = 0 + (4-0)*(1-0)/(2-0) = 2.0
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

    @Test
    void testRemoveFirstElement() {
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0}, new double[]{10, 20, 30}
        );
        func.remove(0);
        assertEquals(2, func.getCount());
        assertEquals(2.0, func.getX(0));
        assertEquals(20, func.getY(0));
        assertEquals(3.0, func.rightBound());
    }

    @Test
    void testRemoveLastElement() {
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0}, new double[]{10, 20, 30}
        );
        func.remove(2);
        assertEquals(2, func.getCount());
        assertEquals(2.0, func.rightBound());
        assertEquals(20, func.getY(1));
    }

    @Test
    void testRemoveMiddleElement() {
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0, 4.0}, new double[]{10, 20, 30, 40}
        );
        func.remove(1); // удаляем x=2.0
        assertEquals(3, func.getCount());
        assertEquals(1.0, func.getX(0));
        assertEquals(3.0, func.getX(1));
        assertEquals(4.0, func.getX(2));
        assertEquals(30, func.getY(1));
    }

    @Test
    void testRemoveThrowsExceptionWhenOnlyTwoPointsLeft() {
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0}, new double[]{10, 20, 30}
        );

        // После удаления останется 2 точки - это допустимо
        func.remove(0);
        assertEquals(2, func.getCount());

        // Теперь попытка удалить еще одну точку должна вызвать исключение
        assertThrows(IllegalStateException.class, () -> func.remove(0));
    }

    @Test
    void testRemoveInvalidIndexThrowsException() {
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0}, new double[]{10, 20}
        );

        assertThrows(IndexOutOfBoundsException.class, () -> func.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> func.remove(2));
        assertThrows(IndexOutOfBoundsException.class, () -> func.remove(5));
    }

    @Test
    void testBoundsAfterRemoval() {
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(
                new double[]{0.0, 1.0, 2.0, 3.0}, new double[]{0, 1, 2, 3}
        );
        func.remove(0); // удаляем левую границу
        assertEquals(1.0, func.leftBound());
        assertEquals(3.0, func.rightBound());

        func.remove(2); // удаляем правую границу (теперь индекс 2 — последний)
        assertEquals(1.0, func.leftBound());
        assertEquals(2.0, func.rightBound());
    }

    @Test
    void testInterpolateXNotEx() {
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(
                new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0}, new double[]{0, 1, 2, 3, 4, 5, 6}
        );
        assertThrows(InterpolationException.class, () -> func.interpolate(2.5, 2));
    }

    @Test
    void testInterpolateXEx() {
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(
                new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0}, new double[]{0, 1, 2, 3, 4, 5, 6}
        );
        assertThrows(InterpolationException.class, () -> func.interpolate(2.5, 3));
    }
}