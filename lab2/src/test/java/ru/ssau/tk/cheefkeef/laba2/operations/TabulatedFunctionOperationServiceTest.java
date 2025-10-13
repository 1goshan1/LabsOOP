package ru.ssau.tk.cheefkeef.laba2.operations;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.cheefkeef.laba2.exceptions.InconsistentFunctionsException;
import ru.ssau.tk.cheefkeef.laba2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.Point;
import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.LinkedListTabulatedFunctionFactory;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.TabulatedFunctionFactory;

import static org.junit.jupiter.api.Assertions.*;

public class TabulatedFunctionOperationServiceTest {
    @Test
    void testAsPointsWithArrayTabulatedFunction() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        TabulatedFunction func = new ArrayTabulatedFunction(x, y);

        Point[] points = TabulatedFunctionOperationService.asPoints(func);

        assertEquals(3, points.length);
        assertEquals(1.0, points[0].x);
        assertEquals(10.0, points[0].y);
        assertEquals(2.0, points[1].x);
        assertEquals(20.0, points[1].y);
        assertEquals(3.0, points[2].x);
        assertEquals(30.0, points[2].y);
    }

    @Test
    public void testAddSameTypeArray() {
        double[] x = {1, 2, 3};
        double[] y1 = {10, 20, 30};
        double[] y2 = {1, 2, 3};

        TabulatedFunction f1 = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction f2 = new ArrayTabulatedFunction(x, y2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunction result = service.add(f1, f2);

        assertEquals(3, result.getCount());
        assertEquals(11, result.getY(0), 1e-10);
        assertEquals(22, result.getY(1), 1e-10);
        assertEquals(33, result.getY(2), 1e-10);
        assertTrue(result instanceof ArrayTabulatedFunction);
    }

    @Test
    public void testSubtractDifferentTypes() {
        double[] x = {0, 1, 2};
        double[] y1 = {5, 10, 15};
        double[] y2 = {1, 2, 3};

        TabulatedFunction f1 = new ArrayTabulatedFunction(x, y1);        // Array
        TabulatedFunction f2 = new LinkedListTabulatedFunction(x, y2);  // LinkedList

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(
                new LinkedListTabulatedFunctionFactory()
        );
        TabulatedFunction result = service.subtract(f1, f2);

        assertEquals(3, result.getCount());
        assertEquals(4, result.getY(0), 1e-10);
        assertEquals(8, result.getY(1), 1e-10);
        assertEquals(12, result.getY(2), 1e-10);
        assertTrue(result instanceof LinkedListTabulatedFunction);
    }

    @Test
    public void testInconsistentCount() {
        TabulatedFunction f1 = new ArrayTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1.0, 2.0, 3.0}
        );
        TabulatedFunction f2 = new ArrayTabulatedFunction(
                new double[]{1.0, 2.0},
                new double[]{1.0, 2.0}
        );

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        assertThrows(InconsistentFunctionsException.class, () -> {
            service.add(f1, f2);
        });
    }

    @Test
    public void testInconsistentXValues() {
        TabulatedFunction f1 = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{10, 20});
        TabulatedFunction f2 = new ArrayTabulatedFunction(new double[]{1, 3}, new double[]{1, 3});

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        assertThrows(InconsistentFunctionsException.class, () -> {
            service.add(f1, f2);
        });
    }

    @Test
    public void testFactorySwitching() {
        double[] x = {1, 2};
        double[] y1 = {10, 20};
        double[] y2 = {1, 2};

        TabulatedFunction f1 = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction f2 = new LinkedListTabulatedFunction(x, y2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        service.setFactory(new LinkedListTabulatedFunctionFactory());

        TabulatedFunction result = service.add(f1, f2);
        assertTrue(result instanceof LinkedListTabulatedFunction);
    }
    @Test
    public void testMultiplySameTypeArray() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y1 = {2.0, 3.0, 4.0};
        double[] y2 = {5.0, 6.0, 7.0};

        TabulatedFunction f1 = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction f2 = new ArrayTabulatedFunction(x, y2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunction result = service.multiply(f1, f2);

        assertEquals(3, result.getCount());
        assertEquals(10.0, result.getY(0), 1e-10);
        assertEquals(18.0, result.getY(1), 1e-10);
        assertEquals(28.0, result.getY(2), 1e-10);
        assertTrue(result instanceof ArrayTabulatedFunction);
    }

    @Test
    public void testMultiplyDifferentTypes() {
        double[] x = {0.0, 1.0, 2.0};
        double[] y1 = {3.0, 4.0, 5.0};  // Array
        double[] y2 = {2.0, 3.0, 4.0};  // LinkedList

        TabulatedFunction f1 = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction f2 = new LinkedListTabulatedFunction(x, y2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(
                new LinkedListTabulatedFunctionFactory()
        );
        TabulatedFunction result = service.multiply(f1, f2);

        assertEquals(3, result.getCount());
        assertEquals(6.0, result.getY(0), 1e-10);
        assertEquals(12.0, result.getY(1), 1e-10);
        assertEquals(20.0, result.getY(2), 1e-10);
        assertTrue(result instanceof LinkedListTabulatedFunction);
    }

    @Test
    public void testDivideValid() {
        double[] x = {1.0, 2.0, 4.0};
        double[] y1 = {10.0, 20.0, 40.0};
        double[] y2 = {2.0, 4.0, 5.0};

        TabulatedFunction f1 = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction f2 = new LinkedListTabulatedFunction(x, y2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunction result = service.divide(f1, f2);

        assertEquals(3, result.getCount());
        assertEquals(5.0, result.getY(0), 1e-10);
        assertEquals(5.0, result.getY(1), 1e-10);
        assertEquals(8.0, result.getY(2), 1e-10);
    }

    @Test
    public void testDivideByZeroThrowsArithmeticException() {
        double[] x = {1.0, 2.0};
        double[] y1 = {10.0, 20.0};
        double[] y2 = {2.0, 0.0}; // деление на ноль во второй точке

        TabulatedFunction f1 = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction f2 = new ArrayTabulatedFunction(x, y2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        assertThrows(ArithmeticException.class, () -> {
            service.divide(f1, f2);
        });
    }

    @Test
    void testGetFactory_returnsDefaultFactory_whenCreatedWithDefaultConstructor() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunctionFactory factory = service.getFactory();

        assertNotNull(factory);
        assertTrue(factory instanceof ArrayTabulatedFunctionFactory);
    }

}
