package ru.ssau.tk.cheefkeef.laba2.io;

import ru.ssau.tk.cheefkeef.laba2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.LinkedListTabulatedFunctionFactory;
import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;

import java.io.*;

public class TabulatedFunctionFileOutputStream {

    public static void main(String[] args) {
        try (
                FileOutputStream fileOut1 = new FileOutputStream("output/array function.bin");
                FileOutputStream fileOut2 = new FileOutputStream("output/linked list function.bin");
                BufferedOutputStream bufferedOut1 = new BufferedOutputStream(fileOut1);
                BufferedOutputStream bufferedOut2 = new BufferedOutputStream(fileOut2)
        ) {
            double[] x = {0.0, 1.0, 2.0, 3.0};
            double[] y = {0.0, 1.0, 4.0, 9.0};

            TabulatedFunction arrayFunction = new ArrayTabulatedFunctionFactory().create(x, y);
            TabulatedFunction linkedListFunction = new LinkedListTabulatedFunctionFactory().create(x, y);

            FunctionsIO.writeTabulatedFunction(bufferedOut1, arrayFunction);
            FunctionsIO.writeTabulatedFunction(bufferedOut2, linkedListFunction);

            System.out.println("Функции успешно записаны в файлы.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}