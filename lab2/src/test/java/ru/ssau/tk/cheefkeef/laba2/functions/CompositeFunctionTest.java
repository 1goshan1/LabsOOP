package ru.ssau.tk.cheefkeef.laba2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompositeFunctionTest {

    @Test
    public void testSqrAfterIdentity() {
        MathFunction identity = new IdentityFunction();
        MathFunction sqr = new SqrFunction();
        CompositeFunction comp = new CompositeFunction(identity, sqr); // sqr(identity(x)) = x^2

        assertEquals(4.0, comp.apply(2.0), 1e-9); // 2^2 = 4
        assertEquals(0.25, comp.apply(0.5), 1e-9); // 0.5^2=0.25
        assertEquals(9.0, comp.apply(-3.0), 1e-9); // -3^2=9
    }

    @Test
    public void testIdentityAfterSqr() {
        MathFunction sqr = new SqrFunction();
        MathFunction identity = new IdentityFunction();
        CompositeFunction comp = new CompositeFunction(sqr, identity); // identity(sqr(x)) = x^2

        assertEquals(16.0, comp.apply(4.0), 1e-9); // логика понятна(см. на 23 строку)
        assertEquals(0.0, comp.apply(0.0), 1e-9);
    }

    @Test
    public void testSqrAfterSqr() {
        MathFunction sqr = new SqrFunction();
        CompositeFunction comp = new CompositeFunction(sqr, sqr); // (x^2)^2 = x^4

        assertEquals(81.0, comp.apply(3.0), 1e-9); // 3^4 = 81
        assertEquals(1.0, comp.apply(1.0), 1e-9); // prost)
        assertEquals(0.0625, comp.apply(0.5), 1e-9); // (0.5^2)^2 = 0.25^2 = 0.0625
    }

    @Test
    public void testConstantAfterSqr() {
        MathFunction sqr = new SqrFunction();
        MathFunction const5 = new ConstantFunction(5.0);
        CompositeFunction comp = new CompositeFunction(sqr, const5); // const5(sqr(x)) = 5

        assertEquals(5.0, comp.apply(10.0), 1e-9);
        assertEquals(5.0, comp.apply(-2.0), 1e-9);
        assertEquals(5.0, comp.apply(0.0), 1e-9);
    }

    @Test
    public void testSqrAfterUnit() {
        MathFunction unit = new UnitFunction(); // const 1
        MathFunction sqr = new SqrFunction(); // x^2
        CompositeFunction comp = new CompositeFunction(unit, sqr); // sqr(unit(x)) = sqr(1) = 1

        assertEquals(1.0, comp.apply(0.0), 1e-9);
        assertEquals(1.0, comp.apply(42.0), 1e-9);
        assertEquals(1.0, comp.apply(-123456789.0), 1e-9);
    }

    @Test
    public void testZeroAfterAnything() {
        MathFunction sqr = new SqrFunction();
        MathFunction zero = new ZeroFunction();
        CompositeFunction comp = new CompositeFunction(sqr, zero); // zero(sqr(x)) = 0

        assertEquals(0.0, comp.apply(7.77), 1e-9);
        assertEquals(0.0, comp.apply(-5.55), 1e-9);
    }

    @Test
    public void testCompositeOfComposites_simple() {
        MathFunction identity = new IdentityFunction();
        MathFunction sqr = new SqrFunction();

        // f(x) = sqr(identity(x)) = x^2
        CompositeFunction f = new CompositeFunction(identity, sqr);

        // g(x) = identity(sqr(x)) = x^2
        CompositeFunction g = new CompositeFunction(sqr, identity);

        // h(x) = g(f(x)) = g(x^2) = (x^2)^2 = x^4
        CompositeFunction h = new CompositeFunction(f, g);

        assertEquals(16.0, h.apply(2.0), 1e-9); // 2^4 = 16
        assertEquals(81.0, h.apply(3.0), 1e-9); // 3^4 = 81
        assertEquals(0.0, h.apply(0.0), 1e-9);
    }

    @Test
    public void testCompositeOfComposites_withConstants() {
        MathFunction unit = new UnitFunction();// const 1
        MathFunction sqr = new SqrFunction(); // x^2
        MathFunction zero = new ZeroFunction(); // const 0

        // f(x) = sqr(unit(x)) = sqr(1) = 1
        CompositeFunction f = new CompositeFunction(unit, sqr);

        // g(x) = zero(sqr(x)) = 0
        CompositeFunction g = new CompositeFunction(sqr, zero);

        // h(x) = g(f(x)) = g(1) = zero(sqr(1)) = zero(1) = 0
        CompositeFunction h = new CompositeFunction(f, g);

        assertEquals(0.0, h.apply(100000.0), 1e-9);
        assertEquals(0.0, h.apply(-987654321.0), 1e-9);
        assertEquals(0.0, h.apply(0.0), 1e-9);
    }

    @Test
    public void testDeepNesting() {
        MathFunction identity = new IdentityFunction();
        MathFunction sqr = new SqrFunction();

        // level1: identity → sqr = x^2
        CompositeFunction level1 = new CompositeFunction(identity, sqr);

        // level2: level1 → level1 = (x^2)^2 = x^4
        CompositeFunction level2 = new CompositeFunction(level1, level1);

        // level3: level2 → sqr = (x^4)^2 = x^8
        CompositeFunction level3 = new CompositeFunction(level2, sqr);

        // x = 2 -> 2^8 = 256
        assertEquals(256.0, level3.apply(2.0), 1e-9);
        // x = 1.5 → (1.5^2)^2 = 1.5^4 = 5.0625; then ^2 → 25.62890625
        assertEquals(Math.pow(1.5, 8), level3.apply(1.5), 1e-9);
    }

    @Test
    public void testIdentityAsNeutralElement() {
        MathFunction sqr = new SqrFunction();
        MathFunction identity = new IdentityFunction();

        CompositeFunction leftId = new CompositeFunction(identity, sqr);  // identity → sqr = sqr
        CompositeFunction rightId = new CompositeFunction(sqr, identity); // sqr → identity = sqr

        double x = 3.14;
        assertEquals(sqr.apply(x), leftId.apply(x), 1e-9);
        assertEquals(sqr.apply(x), rightId.apply(x), 1e-9);
    }
}