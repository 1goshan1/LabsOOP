package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.cheefkeef.laba2.exceptions.ArrayIsNotSortedException;
import ru.ssau.tk.cheefkeef.laba2.exceptions.DifferentLengthOfArraysException;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractTabulatedFunctionTest {

    @Test
    void checkDifferentLength() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {4, 4, 4};

        assertThrows(
                DifferentLengthOfArraysException.class,
                () -> AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues)
        );
    }

    @Test
    void checkSameLength() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {4, 4, 4, 4};

        AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);

        assertEquals(4, 4);
    }

    @Test
    void checkNotSorted() {
        double[] xValues = {1.0, 5.0, 3.0, 4.0};

        assertThrows(
                ArrayIsNotSortedException.class,
                () -> AbstractTabulatedFunction.checkSorted(xValues)
        );
    }

    @Test
    void checkSorted() {
        double[] xValues = {1.0, 3.0, 3.0, 4.0};

        AbstractTabulatedFunction.checkSorted(xValues);

        assertEquals(4, 4);
    }
    @Test
    void testToString_ArrayTabulatedFunction() {
        double[] x = {0.0, 0.5, 1.0};
        double[] y = {0.0, 0.25, 1.0};
        TabulatedFunction func = new ArrayTabulatedFunction(x, y);

        String expected = "ArrayTabulatedFunction size = 3\n[0.0; 0.0]\n[0.5; 0.25]\n[1.0; 1.0]";

        assertEquals(expected, func.toString());
    }

    @Test
    void testToString_LinkedListTabulatedFunction() {
        double[] x = {1.0, 2.0};
        double[] y = {1.0, 4.0};
        TabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        String expected = "LinkedListTabulatedFunction size = 2\n[1.0; 1.0]\n[2.0; 4.0]";

        assertEquals(expected, func.toString());
    }
}
