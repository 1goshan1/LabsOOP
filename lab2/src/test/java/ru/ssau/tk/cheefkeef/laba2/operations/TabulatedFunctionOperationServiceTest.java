package ru.ssau.tk.cheefkeef.laba2.operations;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.cheefkeef.laba2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.Point;
import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;

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
}
