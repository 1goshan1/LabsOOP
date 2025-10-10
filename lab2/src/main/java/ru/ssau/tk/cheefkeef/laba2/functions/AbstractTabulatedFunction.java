package ru.ssau.tk.cheefkeef.laba2.functions;

import ru.ssau.tk.cheefkeef.laba2.exceptions.ArrayIsNotSortedException;
import ru.ssau.tk.cheefkeef.laba2.exceptions.DifferentLengthOfArraysException;

public abstract class AbstractTabulatedFunction implements TabulatedFunction {

    protected abstract int floorIndexOfX(double x);
    protected abstract double extrapolateLeft(double x);
    protected abstract double extrapolateRight(double x);
    protected abstract double interpolate(double x, int floorIndex);

    protected double interpolate(double x, double leftX, double rightX, double leftY, double rightY) {
        return leftY + (rightY - leftY) * (x - leftX) / (rightX - leftX);
    }

    @Override
    public double apply(double x) {
        if (x < leftBound()) {
            return extrapolateLeft(x);
        } else if (x > rightBound()) {
            return extrapolateRight(x);
        } else {
            int idx = indexOfX(x);
            if (idx != -1) {
                return getY(idx);
            } else {
                int floorIdx = floorIndexOfX(x);
                return interpolate(x, floorIdx);
            }
        }
    }

    public static void checkLengthIsTheSame(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length) {
            throw new DifferentLengthOfArraysException("Arrays have different length");
        }
    }

    public static void checkSorted(double[] xValues) {
        for (int i = 0; i < xValues.length - 1; i++) {
            if (xValues[i] > xValues[i+1]) {
                throw new ArrayIsNotSortedException("Array is not sorted");
            }
        }
    }
}