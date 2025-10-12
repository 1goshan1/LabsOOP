package ru.ssau.tk.cheefkeef.laba2.functions.factory;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.cheefkeef.laba2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;

import static org.junit.jupiter.api.Assertions.*;

class TabulatedFunctionFactoryTest {

    @Test
    void testArrayTabulatedFunctionFactory() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.create(xValues, yValues);

        assertNotNull(function);
        assertTrue(function instanceof ArrayTabulatedFunction);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0));
        assertEquals(10.0, function.getY(0));
        assertEquals(3.0, function.getX(2));
        assertEquals(30.0, function.getY(2));
    }

    @Test
    void testLinkedListTabulatedFunctionFactory() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};

        TabulatedFunction function = factory.create(xValues, yValues);

        assertNotNull(function);
        assertTrue(function instanceof LinkedListTabulatedFunction);
        assertEquals(4, function.getCount());
        assertEquals(1.0, function.getX(0));
        assertEquals(1.0, function.getY(0));
        assertEquals(4.0, function.getX(3));
        assertEquals(16.0, function.getY(3));
    }

    @Test
    void testArrayFactoryWithDifferentData() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

        double[] xValues1 = {0.0, 1.0};
        double[] yValues1 = {0.0, 1.0};
        TabulatedFunction function1 = factory.create(xValues1, yValues1);
        assertTrue(function1 instanceof ArrayTabulatedFunction);
        assertEquals(2, function1.getCount());

        double[] xValues2 = {0.0, 0.5, 1.0, 1.5, 2.0};
        double[] yValues2 = {0.0, 0.25, 1.0, 2.25, 4.0};
        TabulatedFunction function2 = factory.create(xValues2, yValues2);
        assertTrue(function2 instanceof ArrayTabulatedFunction);
        assertEquals(5, function2.getCount());
    }

    @Test
    void testLinkedListFactoryWithDifferentData() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();

        double[] xValues1 = {0.0, 1.0};
        double[] yValues1 = {0.0, 1.0};
        TabulatedFunction function1 = factory.create(xValues1, yValues1);
        assertTrue(function1 instanceof LinkedListTabulatedFunction);
        assertEquals(2, function1.getCount());

        double[] xValues2 = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues2 = {4.0, 1.0, 0.0, 1.0, 4.0};
        TabulatedFunction function2 = factory.create(xValues2, yValues2);
        assertTrue(function2 instanceof LinkedListTabulatedFunction);
        assertEquals(5, function2.getCount());
    }

    @Test
    void testFactoryCreatesFunctionalObjects() {
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();

        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 6.0};

        TabulatedFunction arrayFunction = arrayFactory.create(xValues, yValues);
        TabulatedFunction linkedListFunction = linkedListFactory.create(xValues, yValues);

        // Проверяем, что обе функции работают корректно
        assertEquals(2.0, arrayFunction.getY(0));
        assertEquals(4.0, arrayFunction.getY(1));
        assertEquals(6.0, arrayFunction.getY(2));

        assertEquals(2.0, linkedListFunction.getY(0));
        assertEquals(4.0, linkedListFunction.getY(1));
        assertEquals(6.0, linkedListFunction.getY(2));

        // Проверяем границы
        assertEquals(1.0, arrayFunction.leftBound());
        assertEquals(3.0, arrayFunction.rightBound());
        assertEquals(1.0, linkedListFunction.leftBound());
        assertEquals(3.0, linkedListFunction.rightBound());
    }

    @Test
    void testFactoryWithInvalidDataThrowsException() {
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();

        double[] singleX = {1.0};
        double[] singleY = {1.0};

        double[] emptyX = {};
        double[] emptyY = {};

        double[] differentLengthX = {1.0, 2.0};
        double[] differentLengthY = {1.0};

        // Проверяем, что фабрики пробрасывают исключения из конструкторов
        assertThrows(IllegalArgumentException.class, () -> arrayFactory.create(singleX, singleY));
        assertThrows(IllegalArgumentException.class, () -> linkedListFactory.create(singleX, singleY));

        assertThrows(IllegalArgumentException.class, () -> arrayFactory.create(emptyX, emptyY));
        assertThrows(IllegalArgumentException.class, () -> linkedListFactory.create(emptyX, emptyY));

        assertThrows(IllegalArgumentException.class, () -> arrayFactory.create(differentLengthX, differentLengthY));
        assertThrows(IllegalArgumentException.class, () -> linkedListFactory.create(differentLengthX, differentLengthY));
    }

    @Test
    void testFactoryCreatesDistinctInstances() {
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();

        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};

        TabulatedFunction function1 = arrayFactory.create(xValues, yValues);
        TabulatedFunction function2 = arrayFactory.create(xValues, yValues);

        // Проверяем, что создаются разные экземпляры
        assertNotSame(function1, function2);

        function1.setY(0, 100.0);
        assertEquals(1.0, function2.getY(0)); // Вторая функция не изменилась
    }
}