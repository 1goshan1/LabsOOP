package ru.ssau.tk.cheefkeef.laba2.io;

import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.LinkedListTabulatedFunctionFactory;
import ru.ssau.tk.cheefkeef.laba2.operations.TabulatedDifferentialOperator;

import java.io.*;

public class TabulatedFunctionFileInputStream {

    public static void main(String[] args) {
        // Часть 1: Чтение бинарной функции из файла
        try (FileInputStream fileInputStream = new FileInputStream("input/binary function.bin");
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

            TabulatedFunction function = FunctionsIO.readTabulatedFunction(
                    bufferedInputStream,
                    new ArrayTabulatedFunctionFactory()
            );
            System.out.println("Функция из файла:");
            System.out.println(function.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Часть 2: Чтение функции из консоли
        System.out.println("Введите размер и значения функции");
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        try {
            TabulatedFunction consoleFunction = FunctionsIO.readTabulatedFunction(
                    bufferedReader,
                    new LinkedListTabulatedFunctionFactory()
            );

            TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
            TabulatedFunction derivative = differentialOperator.derive(consoleFunction);

            System.out.println("Производная функции:");
            System.out.println(derivative.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}