package ru.ssau.tk.cheefkeef.laba2.exceptions;

public class InconsistentFunctionsException extends RuntimeException {
    public InconsistentFunctionsException() { super(); }
    public InconsistentFunctionsException(String message) {
        super(message);
    }
}
