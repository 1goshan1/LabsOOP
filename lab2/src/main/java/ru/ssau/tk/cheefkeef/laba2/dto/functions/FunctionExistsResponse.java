// FunctionExistsResponse.java
package ru.ssau.tk.cheefkeef.laba2.dto.functions;

public class FunctionExistsResponse {
    private boolean exists;

    // Конструкторы, геттеры и сеттеры
    public FunctionExistsResponse() {}

    public FunctionExistsResponse(boolean exists) {
        this.exists = exists;
    }

    public boolean isExists() { return exists; }
    public void setExists(boolean exists) { this.exists = exists; }
}