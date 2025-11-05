package ru.ssau.tk.cheefkeef.laba2.functions;

import ru.ssau.tk.cheefkeef.laba2.exceptions.InterpolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Arrays;

public class ArrayTabulatedFunction extends AbstractTabulatedFunction implements Removable, Insertable, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ArrayTabulatedFunction.class);

    private double[] xValues;
    private double[] yValues;
    private int count;
    @Serial
    private static final long serialVersionUID = 1601243305971609374L;

    public ArrayTabulatedFunction(double[] xValues, double[] yValues) {
        logger.info("Creating ArrayTabulatedFunction from arrays with length: {}", xValues.length);

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
        logger.debug("Successfully created ArrayTabulatedFunction with {} points", count);
    }

    public ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        logger.info("Creating ArrayTabulatedFunction from function: xFrom={}, xTo={}, count={}", xFrom, xTo, count);

        if (count < 2) throw new IllegalArgumentException("Count is less than minimum");
        if (xFrom > xTo) {
            logger.warn("xFrom ({}) > xTo ({}), swapping values", xFrom, xTo);
            double tmp = xFrom;
            xFrom = xTo;
            xTo = tmp;
        }
        this.count = count;
        this.xValues = new double[count];
        this.yValues = new double[count];
        if (xFrom == xTo) {
            logger.debug("xFrom equals xTo, creating constant function");
            Arrays.fill(xValues, xFrom);
            Arrays.fill(yValues, source.apply(xFrom));
            logger.trace("Constant function: all y values = {}", source.apply(xFrom));
        } else {
            double step = (xTo - xFrom) / (count - 1);
            logger.debug("Step size: {}", step);
            for (int i = 0; i < count; i++) {
                xValues[i] = xFrom + step * i;
                yValues[i] = source.apply(xValues[i]);
                logger.trace("Point {}: x={}, y={}", i, xValues[i], yValues[i]);
            }
        }
        logger.info("Successfully created ArrayTabulatedFunction from source function");
    }

    @Override
    public int getCount() {
        logger.trace("Getting count: {}", count);
        return count;
    }
    // по заданию изменение исключения
    @Override
    public double getX(int index) {
        if (index < 0 || index >= count) {
            logger.error("Index out of bounds: index={}, count={}", index, count);
            throw new IllegalArgumentException("Index is out of bounds");
        }
        double value = xValues[index];
        logger.trace("Getting x[{}] = {}", index, value);
        return value;
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count) {
            logger.error("Index out of bounds: index={}, count={}", index, count);
            throw new IllegalArgumentException("Index is out of bounds");
        }
        double value = yValues[index];
        logger.trace("Getting y[{}] = {}", index, value);
        return value;
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count) {
            logger.error("Index out of bounds: index={}, count={}", index, count);
            throw new IllegalArgumentException("Index is out of bounds");
        }
        double oldValue = yValues[index];
        yValues[index] = value;
        logger.debug("Set y[{}] from {} to {}", index, oldValue, value);
    }

    @Override
    public int indexOfX(double x) {
        for (int i = 0; i < count; i++) {
            if (xValues[i] == x) {
                logger.debug("Found x={} at index {}", x, i);
                return i;
            }
        }
        logger.debug("x={} not found in xValues", x);
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        for (int i = 0; i < count; i++) {
            if (yValues[i] == y) {
                logger.debug("Found y={} at index {}", y, i);
                return i;
            }
        }
        logger.debug("y={} not found in yValues", y);
        return -1;
    }

    @Override
    public double leftBound() {
        double bound = xValues[0];
        logger.trace("Left bound: {}", bound);
        return bound;
    }

    @Override
    public double rightBound() {
        double bound = xValues[count - 1];
        logger.trace("Right bound: {}", bound);
        return bound;
    }

    @Override
    protected int floorIndexOfX(double x) {
        if (x < xValues[0]) {
            logger.error("X value {} is less than left bound {}", x, xValues[0]);
            throw new IllegalArgumentException("X is less than left bound");
        }
        if (x > xValues[count - 1]) return count;
        for (int i = 0; i < count - 1; i++) {
            if (xValues[i] <= x && x < xValues[i + 1]) {
                logger.debug("Floor index for x={} is {}", x, i);
                return i;
            }
        }
        logger.debug("Floor index for x={} is {}", x, count - 1);
        return count - 1;
    }

    @Override
    protected double extrapolateLeft(double x) {
        double result = interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
        logger.debug("Left extrapolation for x={}: result={}", x, result);
        return result;
    }

    @Override
    protected double extrapolateRight(double x) {
        int n = count - 1;
        double result = interpolate(x, xValues[n - 1], xValues[n], yValues[n - 1], yValues[n]);
        logger.debug("Right extrapolation for x={}: result={}", x, result);
        return result;
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        if (floorIndex == count - 1) {
            logger.debug("Interpolation at last point, returning y[{}] = {}", floorIndex, yValues[floorIndex]);
            return yValues[floorIndex];
        }

        // x0 < x < x1 <=> x0 < x and x < x1
        if (!(xValues[floorIndex] <= x && x <= xValues[floorIndex + 1])) {
            logger.error("Interpolation error: x={} not in interval [{}, {}]",
                    x, xValues[floorIndex], xValues[floorIndex + 1]);
            throw new InterpolationException("Illegal x value");
        }

        double result = interpolate(x, xValues[floorIndex], xValues[floorIndex + 1],
                yValues[floorIndex], yValues[floorIndex + 1]);
        logger.debug("Interpolation for x={} at floorIndex={}: result={}", x, floorIndex, result);
        return result;
    }

    @Override
    public void remove(int index) {
        logger.info("Removing point at index {}", index);

        if (count <= 2) {
            logger.error("Cannot remove point - minimum points count reached (current: {})", count);
            throw new IllegalStateException("Cannot remove - minimum points count reached");
        }
        if (index < 0 || index >= count) {
            logger.error("Index out of bounds: index={}, count={}", index, count);
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

        logger.debug("Successfully removed point at index {}. New count: {}", index, count);
    }

    @Override
    public void insert(double x, double y) {
        logger.info("Inserting point: x={}, y={}", x, y);

        int existingIndex = indexOfX(x);
        if (existingIndex != -1) {
            logger.debug("Point with x={} already exists at index {}, updating y value", x, existingIndex);
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

        logger.debug("Successfully inserted point at index {}. New count: {}", insertIndex, count);
    }

    @Override
    public Iterator<Point> iterator() {
        logger.trace("Creating iterator for ArrayTabulatedFunction");
        return new Iterator<Point>() {
            private int i = 0; // текущий индекс

            @Override
            public boolean hasNext() {
                boolean hasNext = i < count;
                logger.trace("Iterator hasNext: {} (i={}, count={})", hasNext, i, count);
                return hasNext;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    logger.error("Iterator has no more elements");
                    throw new java.util.NoSuchElementException();
                }
                Point point = new Point(xValues[i], yValues[i]);
                logger.trace("Iterator next: point[{}] = ({}, {})", i, point.x, point.y);
                i++;
                return point;
            }
        };
    }
}