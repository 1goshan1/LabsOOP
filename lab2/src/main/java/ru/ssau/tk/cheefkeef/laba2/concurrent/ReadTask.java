package ru.ssau.tk.cheefkeef.laba2.concurrent;


import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;

public class ReadTask implements Runnable {
    private final TabulatedFunction function;

    public ReadTask(TabulatedFunction function) {
        this.function = function;
    }

    @Override
    public void run() {
        int count = function.getCount();
        for (int i = 0; i < count; ++i) {
                double x = function.getX(i);
                double y = function.getY(i);
                System.out.printf("After read: i = %d, x = %f, y = %f%n", i, x, y); // %d = i, %f = x, next %f = y, %n = \n no diff
        }
    }
}