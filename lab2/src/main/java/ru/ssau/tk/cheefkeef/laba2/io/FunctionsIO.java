package ru.ssau.tk.cheefkeef.laba2.io;

import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.TabulatedFunctionFactory;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public final class FunctionsIO {

    private FunctionsIO() {
        throw new UnsupportedOperationException("Utility class FunctionsIO cannot be instantiated");
    }


    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) throws IOException {
        PrintWriter printWriter = new PrintWriter(writer);
        int count = function.getCount();
        printWriter.println(count);

        for (var point : function) {
            printWriter.printf("%f %f\n", point.x, point.y);
        }

        printWriter.flush();
    }

    public static TabulatedFunction readTabulatedFunction(BufferedReader reader, TabulatedFunctionFactory factory)
            throws IOException {
        String countLine = reader.readLine();
        if (countLine == null) {
            throw new IOException("File is empty");
        }

        int count;
        try {
            count = Integer.parseInt(countLine.trim());
        } catch (NumberFormatException e) {
            throw new IOException("Invalid count format: " + countLine, e);
        }

        if (count <= 0) {
            throw new IOException("Count must be positive");
        }

        double[] xValues = new double[count];
        double[] yValues = new double[count];

        // Создаём форматтер для русской локали (запятая как десятичный разделитель)
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.forLanguageTag("ru"));

        for (int i = 0; i < count; i++) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("Unexpected end of file at line " + (i + 2));
            }

            String[] parts = line.trim().split(" ");
            if (parts.length != 2) {
                throw new IOException("Invalid line format at line " + (i + 2) + ": " + line);
            }

            try {
                xValues[i] = numberFormat.parse(parts[0]).doubleValue();
                yValues[i] = numberFormat.parse(parts[1]).doubleValue();
            } catch (ParseException e) {
                throw new IOException("Failed to parse number at line " + (i + 2) + ": " + line, e);
            }
        }

        return factory.create(xValues, yValues);
    }

    public static void serialize(BufferedOutputStream stream, TabulatedFunction function) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);
        objectOutputStream.writeObject(function);
        objectOutputStream.flush();
    }

    public static TabulatedFunction deserialize(BufferedInputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(stream);
        return (TabulatedFunction) objectInputStream.readObject();
    }


}