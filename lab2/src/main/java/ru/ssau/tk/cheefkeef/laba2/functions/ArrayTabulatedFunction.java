package ru.ssau.tk.cheefkeef.laba2.functions;

import ru.ssau.tk.cheefkeef.laba2.exceptions.InterpolationException;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Arrays;

public class ArrayTabulatedFunction extends AbstractTabulatedFunction implements Removable, Insertable, Serializable {
    private double[] xValues;
    private double[] yValues;
    private int count;

    private static final long serialVersionUID = 1601243305971609374L;

    public ArrayTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length || xValues.length == 0)
            throw new IllegalArgumentException("Arrays must be non-empty and of equal length");
        AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);
        if (xValues.length < 2) {
            throw new IllegalArgumentException("Length is less than minimum");
        }
        AbstractTabulatedFunction.checkSorted(xValues);

        this.count = xValues.length;
        this.xValues = Arrays.copyOf(xValues, count);
        this.yValues = Arrays.copyOf(yValues, count);
    }

    public ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count < 2) throw new IllegalArgumentException("Count is less than minimum");
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
    // по заданию изменение исключения
    @Override
    public double getX(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index is out of bounds");
        }
        return xValues[index];
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index is out of bounds");
        }
        return yValues[index];
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index is out of bounds");
        }
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
        if (x < xValues[0]) throw new IllegalArgumentException("X is less than left bound");
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

        // x0 < x < x1 <=> x0 < x and x < x1
        if (!(xValues[floorIndex] <= x && x <= xValues[floorIndex + 1])) {
            throw new InterpolationException("Illegal x value");
        }

        return interpolate(x, xValues[floorIndex], xValues[floorIndex + 1],
                yValues[floorIndex], yValues[floorIndex + 1]);
    }

    @Override
    public void remove(int index) {
        if (count <= 2) {
            throw new IllegalStateException("Cannot remove - minimum points count reached");
        }
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index is out of bounds"); // изменил исключение по заданию
        }
        double[] newX = new double[count - 1];
        double[] newY = new double[count - 1];

        System.arraycopy(xValues, 0, newX, 0, index);
        System.arraycopy(yValues, 0, newY, 0, index);

        System.arraycopy(xValues, index + 1, newX, index, count - index - 1);
        System.arraycopy(yValues, index + 1, newY, index, count - index - 1);

        xValues = newX;
        yValues = newY;
        count--;
    }
    @Override
    public void insert(double x, double y) {
        int existingIndex = indexOfX(x);
        if (existingIndex != -1) {
            yValues[existingIndex] = y;
            return;
        }

        int insertIndex = 0;
        while (insertIndex < count && xValues[insertIndex] < x) {
            insertIndex++;
        }

        double[] newX = new double[count + 1];
        double[] newY = new double[count + 1];

        System.arraycopy(xValues, 0, newX, 0, insertIndex);
        System.arraycopy(yValues, 0, newY, 0, insertIndex);

        newX[insertIndex] = x;
        newY[insertIndex] = y;

        System.arraycopy(xValues, insertIndex, newX, insertIndex + 1, count - insertIndex);
        System.arraycopy(yValues, insertIndex, newY, insertIndex + 1, count - insertIndex);

        xValues = newX;
        yValues = newY;
        count++;
    }

    @Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private int i = 0; // текущий индекс

            @Override
            public boolean hasNext() {
                return i < count;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                Point point = new Point(xValues[i], yValues[i]);
                i++;
                return point;
            }
        };
    }
}