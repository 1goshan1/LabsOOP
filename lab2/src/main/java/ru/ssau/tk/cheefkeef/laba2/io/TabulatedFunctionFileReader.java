package ru.ssau.tk.cheefkeef.laba2.io;

import ru.ssau.tk.cheefkeef.laba2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.LinkedListTabulatedFunctionFactory;
import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.TabulatedFunctionFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TabulatedFunctionFileReader {

    public static void main(String[] args) {
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();

        try (
                BufferedReader arrayReader = new BufferedReader(
                        new FileReader("input/function.txt")
                );
                BufferedReader linkedListReader = new BufferedReader(
                        new FileReader("input/function.txt")
                )
        ) {
            TabulatedFunction arrayFunc = FunctionsIO.readTabulatedFunction(arrayReader, arrayFactory);
            TabulatedFunction linkedListFunc = FunctionsIO.readTabulatedFunction(linkedListReader, linkedListFactory);

            System.out.println("Array function:");
            System.out.println(arrayFunc);

            System.out.println("\nLinked list function:");
            System.out.println(linkedListFunc);

        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}