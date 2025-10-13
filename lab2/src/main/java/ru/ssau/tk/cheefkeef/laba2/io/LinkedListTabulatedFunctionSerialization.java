package ru.ssau.tk.cheefkeef.laba2.io;

import ru.ssau.tk.cheefkeef.laba2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.operations.TabulatedDifferentialOperator;

import java.io.*;

public class LinkedListTabulatedFunctionSerialization {

    public static void main(String[] args) {
        String filePath = "output/serialized linked list functions.bin";

        // Часть 1: Сериализация — запись трёх функций в файл
        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            double[] x = {0.0, 0.5, 1.0, 1.5, 2.0};
            double[] y = {0.0, 0.25, 1.0, 2.25, 4.0};
            TabulatedFunction original = new LinkedListTabulatedFunction(x, y);

            TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
            TabulatedFunction firstDerivative = operator.derive(original);
            TabulatedFunction secondDerivative = operator.derive(firstDerivative);

            // Сериализуем все три функции в один поток
            FunctionsIO.serialize(bos, original);
            FunctionsIO.serialize(bos, firstDerivative);
            FunctionsIO.serialize(bos, secondDerivative);

            System.out.println("Три функции успешно сериализованы в файл: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Часть 2: Десериализация — чтение трёх функций из файла
        try (FileInputStream fis = new FileInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            TabulatedFunction restoredOriginal = FunctionsIO.deserialize(bis);
            TabulatedFunction restoredFirst = FunctionsIO.deserialize(bis);
            TabulatedFunction restoredSecond = FunctionsIO.deserialize(bis);

            System.out.println("\n=== Восстановленные функции ===");
            System.out.println("Оригинал:");
            System.out.println(restoredOriginal.toString());
            System.out.println("\nПервая производная:");
            System.out.println(restoredFirst.toString());
            System.out.println("\nВторая производная:");
            System.out.println(restoredSecond.toString());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}