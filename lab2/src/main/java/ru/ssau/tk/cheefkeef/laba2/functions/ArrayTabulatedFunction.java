package ru.ssau.tk.cheefkeef.laba2.functions;

import java.util.Arrays;

public class ArrayTabulatedFunction extends AbstractTabulatedFunction {
    private final double[] xValues;
    private final double[] yValues;
    private final int count;

    public ArrayTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length || xValues.length == 0)
            throw new IllegalArgumentException("Arrays must be non-empty and of equal length");
        this.count = xValues.length;
        this.xValues = Arrays.copyOf(xValues, count);
        this.yValues = Arrays.copyOf(yValues, count);
    }

    public ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count <= 0) throw new IllegalArgumentException("Count must be positive");
        if (xFrom > xTo) {
            double tmp = xFrom;
            xFrom = xTo;
            xTo = tmp;
        }
        this.count = count;
        this.xValues = new double[count];
        this.yValues = new double[count];
        if (xFrom == xTo) {
            Arrays.fill(xValues, xFrom);
            Arrays.fill(yValues, source.apply(xFrom));
        } else {
            double step = (xTo - xFrom) / (count - 1);
            for (int i = 0; i < count; i++) {
                xValues[i] = xFrom + step * i;
                yValues[i] = source.apply(xValues[i]);
            }
        }
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        if (index < 0 || index >= count) throw new IndexOutOfBoundsException();
        return xValues[index];
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count) throw new IndexOutOfBoundsException();
        return yValues[index];
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count) throw new IndexOutOfBoundsException();
        yValues[index] = value;
    }

    @Override
    public int indexOfX(double x) {
        for (int i = 0; i < count; i++) {
            if (xValues[i] == x) return i;
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        for (int i = 0; i < count; i++) {
            if (yValues[i] == y) return i;
        }
        return -1;
    }

    @Override
    public double leftBound() {
        return xValues[0];
    }

    @Override
    public double rightBound() {
        return xValues[count - 1];
    }

    @Override
    protected int floorIndexOfX(double x) {
        if (x < xValues[0]) return 0;
        if (x > xValues[count - 1]) return count;
        for (int i = 0; i < count - 1; i++) {
            if (xValues[i] <= x && x < xValues[i + 1]) {
                return i;
            }
        }
        return count - 1;
    }

    @Override
    protected double extrapolateLeft(double x) {
        return interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
    }

    @Override
    protected double extrapolateRight(double x) {
        int n = count - 1;
        return interpolate(x, xValues[n - 1], xValues[n], yValues[n - 1], yValues[n]);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        if (floorIndex == count - 1) {
            return yValues[floorIndex];
        }
        return interpolate(x, xValues[floorIndex], xValues[floorIndex + 1],
                yValues[floorIndex], yValues[floorIndex + 1]);
    }
}