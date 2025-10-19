package ru.ssau.tk.cheefkeef.laba2.concurrent;

import ru.ssau.tk.cheefkeef.laba2.functions.ConstantFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;

public class ReadWriteTaskExecutor {

    public static void main(String[] args) {
        ConstantFunction constantNegative = new ConstantFunction(-1); // -1 тк в задании как пример указано, ну а зачем думать, когда можно не думать
        TabulatedFunction tabulatedFunction = new LinkedListTabulatedFunction(constantNegative, 1.0, 1000.0, 1000);

        ReadTask readTask = new ReadTask(tabulatedFunction);
        WriteTask writeTask = new WriteTask(tabulatedFunction, 0.5); // аналогичное число из задания

        Thread readerThread = new Thread(readTask);
        Thread writerThread = new Thread(writeTask);

        readerThread.start();
        writerThread.start();

        try {
            readerThread.join();   // ждём завершения чтения
            writerThread.join();   // ждём завершения записи
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}