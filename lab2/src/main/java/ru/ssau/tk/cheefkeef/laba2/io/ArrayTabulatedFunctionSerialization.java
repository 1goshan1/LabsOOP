package ru.ssau.tk.cheefkeef.laba2.io;

import ru.ssau.tk.cheefkeef.laba2.functions.*;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.cheefkeef.laba2.operations.TabulatedDifferentialOperator;

import java.io.*;

public class ArrayTabulatedFunctionSerialization {

    public static void main(String[] args) {
        String filePath = "output/serialized array functions.bin";

        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(filePath))) {

            TabulatedFunction original = new ArrayTabulatedFunction(
                    new double[]{0.0, 1.0, 2.0, 3.0, 4.0},
                    new double[]{0.0, 1.0, 4.0, 9.0, 16.0}
            );

            TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(
                    new ArrayTabulatedFunctionFactory()
            );

            TabulatedFunction firstDerivative = operator.derive(original);
            TabulatedFunction secondDerivative = operator.derive(firstDerivative);

            FunctionsIO.serialize(bos, original);
            FunctionsIO.serialize(bos, firstDerivative);
            FunctionsIO.serialize(bos, secondDerivative);

            System.out.println("Сериализация завершена. Файл: " + filePath);

        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        try (BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(filePath))) {

            TabulatedFunction original = (ArrayTabulatedFunction) FunctionsIO.deserialize(bis);
            TabulatedFunction firstDerivative = (ArrayTabulatedFunction) FunctionsIO.deserialize(bis);
            TabulatedFunction secondDerivative = (ArrayTabulatedFunction) FunctionsIO.deserialize(bis);

            System.out.println("\n=== Оригинал ===");
            System.out.println(original);

            System.out.println("\n=== Первая производная ===");
            System.out.println(firstDerivative);

            System.out.println("\n=== Вторая производная ===");
            System.out.println(secondDerivative);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace(System.err);
        }
    }
}