// FunctionCountResponse.java
package ru.ssau.tk.cheefkeef.laba2.dto.functions;

public class FunctionCountResponse {
    private int count;

    // Конструкторы, геттеры и сеттеры
    public FunctionCountResponse() {}

    public FunctionCountResponse(int count) {
        this.count = count;
    }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}