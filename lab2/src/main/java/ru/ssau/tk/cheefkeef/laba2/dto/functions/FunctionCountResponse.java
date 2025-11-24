package ru.ssau.tk.cheefkeef.laba2.dto.functions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FunctionCountResponse {

    @JsonProperty("count")
    private int count;

    // Конструкторы
    public FunctionCountResponse() {}

    public FunctionCountResponse(int count) {
        this.count = count;
    }

    // Геттеры и сеттеры
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}