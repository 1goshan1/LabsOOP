package ru.ssau.tk.cheefkeef.laba2.functions;

public class SimpleIterativeMethod implements MathFunction { // I am not sure about this one, cause the task is MathFunction, but method is not a function imo
    private final MathFunction iterationFunction;
    private final double tolerance;
    private final int maxIterations;

    public SimpleIterativeMethod(MathFunction iterationFunction, double tolerance, int maxIterations) {
        this.iterationFunction = iterationFunction;
        this.tolerance = tolerance;
        this.maxIterations = maxIterations;
    }

    @Override
    public double apply(double initialGuess) {
        return findRoot(initialGuess);
    }

    public double findRoot(double initialGuess) {
        double current = initialGuess;
        int iteration = 0;

        while (iteration < maxIterations) {
            double next = iterationFunction.apply(current);

            if (Math.abs(next - current) <= tolerance) {
                return next; // answer
            }

            current = next;
            iteration++;
        }

        // if iteration exceeds maxIterations
        return current;
    }
}