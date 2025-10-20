package ru.ssau.tk.cheefkeef.laba2.concurrent;

import ru.ssau.tk.cheefkeef.laba2.functions.*;

import java.util.ArrayList;
import java.util.List;

public class MultiplyingTaskExecutor {

    public static void main(String[] args) throws InterruptedException {
        // Создаём функцию: тождественная 1 на [1, 1000] с 1000 точками
        TabulatedFunction func = new LinkedListTabulatedFunction(
                new UnitFunction(),
                1.0,
                1000.0,
                1000
        );

        int threadCount = 10;
        List<Thread> threads = new ArrayList<>();

        // Создаём потоки
        for (int i = 0; i < threadCount; i++) {
            MultiplyingTask task = new MultiplyingTask(func);
            Thread thread = new Thread(task, "Multiplier-" + i);
            threads.add(thread);
        }

        // Запускаем все потоки
        for (Thread thread : threads) {
            thread.start();
        }

        // Даём время на выполнение (2 секунды)
        Thread.sleep(2000);

        // Выводим результат
        System.out.println("\nРезультат после умножения:");
        System.out.println("Первое значение y: " + func.getY(0));
        System.out.println("Ожидаемое значение: " + Math.pow(2, threadCount)); // 2^10 = 1024

        // Проверка
        double expected = Math.pow(2, threadCount);
        if (Math.abs(func.getY(0) - expected) < 1e-6) {
            System.out.println("Результат корректен");
        } else {
            System.out.println("Результат некорректен");
        }
    }
}