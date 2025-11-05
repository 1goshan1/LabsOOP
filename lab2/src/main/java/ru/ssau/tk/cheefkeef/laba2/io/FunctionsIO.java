package ru.ssau.tk.cheefkeef.laba2.io;

import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.TabulatedFunctionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public final class FunctionsIO {
    private static final Logger logger = LoggerFactory.getLogger(FunctionsIO.class);

    private FunctionsIO() {
        throw new UnsupportedOperationException("Utility class FunctionsIO cannot be instantiated");
    }

    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) throws IOException {
        logger.info("Starting tabulated function write to BufferedWriter");
        PrintWriter printWriter = new PrintWriter(writer);
        int count = function.getCount();
        logger.debug("Function count: {}", count);
        printWriter.println(count);

        for (var point : function) {
            printWriter.printf("%f %f\n", point.x, point.y);
            logger.trace("Written point: x={}, y={}", point.x, point.y);
        }

        printWriter.flush();
        logger.info("Successfully wrote {} points to BufferedWriter", count);
    }

    public static void writeTabulatedFunction(BufferedOutputStream outputStream, TabulatedFunction function)
            throws IOException {
        logger.info("Starting tabulated function write to BufferedOutputStream");
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        int count = function.getCount();
        logger.debug("Function count: {}", count);
        dataOutputStream.writeInt(count);

        for (int i = 0; i < count; i++) {
            dataOutputStream.writeDouble(function.getX(i));
            dataOutputStream.writeDouble(function.getY(i));
            logger.trace("Written point {}: x={}, y={}", i, function.getX(i), function.getY(i));
        }

        dataOutputStream.flush();
        logger.info("Successfully wrote {} points to BufferedOutputStream", count);
    }

    public static TabulatedFunction readTabulatedFunction(BufferedReader reader, TabulatedFunctionFactory factory)
            throws IOException {
        logger.info("Starting tabulated function read from BufferedReader");
        String countLine = reader.readLine();
        if (countLine == null) {
            logger.error("File is empty - cannot read count");
            throw new IOException("File is empty");
        }

        int count;
        try {
            count = Integer.parseInt(countLine.trim());
            logger.debug("Reading {} points", count);
        } catch (NumberFormatException e) {
            logger.error("Invalid count format: '{}'", countLine, e);
            throw new IOException("Invalid count format: " + countLine, e);
        }

        if (count <= 0) {
            logger.error("Invalid count value: {}", count);
            throw new IOException("Count must be positive");
        }

        double[] xValues = new double[count];
        double[] yValues = new double[count];

        // Создаём форматтер для русской локали (запятая как десятичный разделитель)
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.forLanguageTag("ru"));
        logger.debug("Using Russian locale for number parsing");

        for (int i = 0; i < count; i++) {
            String line = reader.readLine();
            if (line == null) {
                logger.error("Unexpected end of file at line {}", i + 2);
                throw new IOException("Unexpected end of file at line " + (i + 2));
            }

            String[] parts = line.trim().split(" ");
            if (parts.length != 2) {
                logger.error("Invalid line format at line {}: '{}'", i + 2, line);
                throw new IOException("Invalid line format at line " + (i + 2) + ": " + line);
            }

            try {
                xValues[i] = numberFormat.parse(parts[0]).doubleValue();
                yValues[i] = numberFormat.parse(parts[1]).doubleValue();
                logger.trace("Parsed point {}: x={}, y={}", i, xValues[i], yValues[i]);
            } catch (ParseException e) {
                logger.error("Failed to parse numbers at line {}: '{}'", i + 2, line, e);
                throw new IOException("Failed to parse number at line " + (i + 2) + ": " + line, e);
            }
        }

        TabulatedFunction result = factory.create(xValues, yValues);
        logger.info("Successfully created tabulated function with {} points", count);
        return result;
    }

    public static TabulatedFunction readTabulatedFunction(BufferedInputStream inputStream, TabulatedFunctionFactory factory)
            throws IOException {
        logger.info("Starting tabulated function read from BufferedInputStream");
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int count = dataInputStream.readInt();
        logger.debug("Reading {} points", count);

        if (count <= 0) {
            logger.error("Invalid count value: {}", count);
            throw new IOException("Count must be positive");
        }

        double[] xValues = new double[count];
        double[] yValues = new double[count];

        for (int i = 0; i < count; i++) {
            xValues[i] = dataInputStream.readDouble();
            yValues[i] = dataInputStream.readDouble();
            logger.trace("Read point {}: x={}, y={}", i, xValues[i], yValues[i]);
        }

        TabulatedFunction result = factory.create(xValues, yValues);
        logger.info("Successfully created tabulated function with {} points", count);
        return result;
    }

    public static void serialize(BufferedOutputStream stream, TabulatedFunction function) throws IOException {
        logger.info("Starting serialization of tabulated function");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);
        objectOutputStream.writeObject(function);
        objectOutputStream.flush();
        logger.info("Successfully serialized tabulated function");
    }

    public static TabulatedFunction deserialize(BufferedInputStream stream) throws IOException, ClassNotFoundException {
        logger.info("Starting deserialization of tabulated function");
        ObjectInputStream objectInputStream = new ObjectInputStream(stream);
        TabulatedFunction result = (TabulatedFunction) objectInputStream.readObject();
        logger.info("Successfully deserialized tabulated function with {} points", result.getCount());
        return result;
    }
}