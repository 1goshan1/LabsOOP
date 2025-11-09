// UpdateSignatureRequest.java
package ru.ssau.tk.cheefkeef.laba2.dto.functions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateSignatureRequest {
    @NotBlank(message = "Сигнатура функции не может быть пустой")
    @Size(min = 1, message = "Сигнатура функции должна содержать хотя бы 1 символ")
    private String signature;

    // Конструкторы, геттеры и сеттеры
    public UpdateSignatureRequest() {}

    public UpdateSignatureRequest(String signature) {
        this.signature = signature;
    }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}