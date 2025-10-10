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
}
