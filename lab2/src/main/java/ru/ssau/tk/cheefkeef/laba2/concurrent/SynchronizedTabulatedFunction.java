package ru.ssau.tk.cheefkeef.laba2.concurrent;

import ru.ssau.tk.cheefkeef.laba2.functions.Point;
import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;

import java.util.Iterator;
import java.util.Objects;

public class SynchronizedTabulatedFunction implements TabulatedFunction {

    private final TabulatedFunction delegate;
    private final Object mutex;

    public SynchronizedTabulatedFunction(TabulatedFunction delegate) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate must not be null");
        this.mutex = this;
    }

    @Override
    public synchronized int getCount() {
        return delegate.getCount();
    }

    @Override
    public synchronized double getX(int index) {
        return delegate.getX(index);
    }

    @Override
    public synchronized double getY(int index) {
        return delegate.getY(index);
    }

    @Override
    public synchronized void setY(int index, double value) {
        delegate.setY(index, value);
    }

    @Override
    public synchronized int indexOfX(double x) {
        return delegate.indexOfX(x);
    }

    @Override
    public synchronized int indexOfY(double y) {
        return delegate.indexOfY(y);
    }

    @Override
    public synchronized double leftBound() {
        return delegate.leftBound();
    }

    @Override
    public synchronized double rightBound() {
        return delegate.rightBound();
    }

    @Override
    public synchronized double apply(double x) {
        return delegate.apply(x);
    }

    @Override
    public synchronized Iterator<Point> iterator() {
        return delegate.iterator();
    }

    @Override
    public boolean equals(Object obj) {
        synchronized (mutex) {
            return delegate.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        synchronized (mutex) {
            return delegate.hashCode();
        }
    }

    @Override
    public String toString() {
        synchronized (mutex) {
            return delegate.toString();
        }
    }
}