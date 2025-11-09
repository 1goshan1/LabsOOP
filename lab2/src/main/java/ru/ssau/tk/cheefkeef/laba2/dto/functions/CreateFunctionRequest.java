// CreateFunctionRequest.java
package ru.ssau.tk.cheefkeef.laba2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateFunctionRequest {
    @NotBlank(message = "Название функции не может быть пустым")
    @Size(min = 1, message = "Название функции должно содержать хотя бы 1 символ")
    private String name;

    @NotBlank(message = "Сигнатура функции не может быть пустой")
    @Size(min = 1, message = "Сигнатура функции должна содержать хотя бы 1 символ")
    private String signature;

    // Конструкторы, геттеры и сеттеры
    public CreateFunctionRequest() {}

    public CreateFunctionRequest(String name, String signature) {
        this.name = name;
        this.signature = signature;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}