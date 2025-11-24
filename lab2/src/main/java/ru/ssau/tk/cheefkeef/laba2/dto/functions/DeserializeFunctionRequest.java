package ru.ssau.tk.cheefkeef.laba2.dto.functions;

import jakarta.validation.constraints.NotBlank;

public class DeserializeFunctionRequest {
    @NotBlank(message = "Сериализованная строка не может быть пустой")
    private String serializedFunction;

    public String getSerializedFunction() {
        return serializedFunction;
    }

    public void setSerializedFunction(String serializedFunction) {
        this.serializedFunction = serializedFunction;
    }
}