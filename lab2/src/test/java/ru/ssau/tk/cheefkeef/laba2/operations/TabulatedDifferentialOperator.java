package ru.ssau.tk.cheefkeef.laba2.operations;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.cheefkeef.laba2.functions.*;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.*;

import static org.junit.jupiter.api.Assertions.*;

class TabulatedDifferentialOperatorTest {

    @Test
    void testDeriveLinearFunction() {
        // f(x) = 2x + 1 → f'(x) = 2 (везде)
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {1.0, 3.0, 5.0, 7.0};

        TabulatedFunction linearFunction = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunction derivative = operator.derive(linearFunction);

        assertEquals(4, derivative.getCount());

        // Для линейной функции все разностные производные = 2
        assertEquals(2.0, derivative.getY(0), 1e-10); // правая: (3-1)/(1-0) = 2
        assertEquals(2.0, derivative.getY(1), 1e-10); // центральная: (5-1)/(2-0) = 4/2 = 2
        assertEquals(2.0, derivative.getY(2), 1e-10); // центральная: (7-3)/(3-1) = 4/2 = 2
        assertEquals(2.0, derivative.getY(3), 1e-10); // левая: (7-5)/(3-2) = 2
    }

    @Test
    void testDeriveQuadraticFunction() {
        // f(x) = x^2 → f'(x) = 2x
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};

        TabulatedFunction quadraticFunction = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunction derivative = operator.derive(quadraticFunction);

        assertEquals(4, derivative.getCount());

        // Правильные значения по формулам:
        assertEquals(1.0, derivative.getY(0), 1e-10); // правая: (1-0)/(1-0) = 1
        assertEquals(2.0, derivative.getY(1), 1e-10); // центральная: (4-0)/(2-0) = 4/2 = 2
        assertEquals(4.0, derivative.getY(2), 1e-10); // центральная: (9-1)/(3-1) = 8/2 = 4
        assertEquals(5.0, derivative.getY(3), 1e-10); // левая: (9-4)/(3-2) = 5
    }

    @Test
    void testDeriveWithTwoPoints() {
        double[] xValues = {0.0, 1.0};
        double[] yValues = {0.0, 1.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunction derivative = operator.derive(function);

        assertEquals(2, derivative.getCount());
        assertEquals(1.0, derivative.getY(0), 1e-10); // (1-0)/(1-0)
        assertEquals(1.0, derivative.getY(1), 1e-10); // (1-0)/(1-0)
    }

    @Test
    void testDeriveWithThreePoints() {
        // f(x) = x^3 в точках 0, 1, 2 → f'(x) = 3x^2 → [0, 3, 12]
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 8.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunction derivative = operator.derive(function);

        assertEquals(3, derivative.getCount());
        //  Методом пристального взгляда можно заметить, что расхождения не то что большие, а прям ну очень большие
        assertEquals(1.0, derivative.getY(0), 1e-10); // правая: (1-0)/(1-0) = 1
        assertEquals(4.0, derivative.getY(1), 1e-10); // центральная: (8-0)/(2-0) = 8/2 = 4
        assertEquals(7.0, derivative.getY(2), 1e-10); // левая: (8-1)/(2-1) = 7
    }

    @Test
    void testDeriveConstantFunction() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {5.0, 5.0, 5.0, 5.0};

        TabulatedFunction constantFunction = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunction derivative = operator.derive(constantFunction);

        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(0.0, derivative.getY(i), 1e-10);
        }
    }

    @Test
    void testDerivePreservesXValues() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunction derivative = operator.derive(function);

        for (int i = 0; i < xValues.length; i++) {
            assertEquals(xValues[i], derivative.getX(i), 1e-10);
        }
    }

    @Test
    void testDeriveWithLinkedListFactory() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};

        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedFunction function = factory.create(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);
        TabulatedFunction derivative = operator.derive(function);

        assertTrue(derivative instanceof LinkedListTabulatedFunction);
        assertEquals(3, derivative.getCount());

        assertEquals(1.0, derivative.getY(0), 1e-10); // правая
        assertEquals(2.0, derivative.getY(1), 1e-10); // центральная: (4-0)/(2-0)=2
        assertEquals(3.0, derivative.getY(2), 1e-10); // левая: (4-1)/(2-1)=3
    }

    @Test
    void testDeriveWithUnevenSpacing() {
        double[] xValues = {0.0, 0.5, 2.0};
        double[] yValues = {0.0, 0.25, 4.0}; // f(x) = x^2

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunction derivative = operator.derive(function);

        assertEquals(3, derivative.getCount());

        assertEquals(0.5, derivative.getY(0), 1e-10); // (0.25-0)/(0.5-0) = 0.5
        assertEquals(2.0, derivative.getY(1), 1e-10); // (4 - 0)/(2 - 0) = 2
        assertEquals(2.5, derivative.getY(2), 1e-10); // (4 - 0.25)/(2 - 0.5) = 2.5
    }

    @Test
    void testDeriveNullFunction() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        assertThrows(IllegalArgumentException.class, () -> operator.derive(null));
    }

    @Test
    void testDefaultConstructorUsesArrayFactory() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        assertNotNull(operator.getFactory());
        assertTrue(operator.getFactory() instanceof ArrayTabulatedFunctionFactory);
    }

    @Test
    void testSetFactory() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();

        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();
        operator.setFactory(linkedListFactory);

        TabulatedFunction derivative = operator.derive(function);

        assertTrue(derivative instanceof LinkedListTabulatedFunction);
    }
}