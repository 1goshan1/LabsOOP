package ru.ssau.tk.cheefkeef.laba2.concurrent;

import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;

public class MultiplyingTask implements Runnable {

    private final TabulatedFunction func;

    public MultiplyingTask(TabulatedFunction func) {
        this.func = func;
    }

    @Override
    public void run() {
        int count = func.getCount();
        for (int i = 0; i < count; i++) {
            synchronized (func) {
                double y = func.getY(i);
                func.setY(i, y * 2);
            }
        }
        System.out.println("Поток " + Thread.currentThread().getName() + " завершил выполнение задачи.");
    }
}