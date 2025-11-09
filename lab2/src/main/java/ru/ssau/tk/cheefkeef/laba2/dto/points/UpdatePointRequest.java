// UpdatePointRequest.java
package ru.ssau.tk.cheefkeef.laba2.dto.points;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdatePointRequest {
    @NotNull(message = "ID функции не может быть null")
    @Min(value = 1, message = "ID функции должен быть не менее 1")
    private Long functionId;

    @NotNull(message = "Координата X не может быть null")
    private Double xValue;

    @NotNull(message = "Координата Y не может быть null")
    private Double yValue;

    // Конструкторы, геттеры и сеттеры
    public UpdatePointRequest() {}

    public UpdatePointRequest(Long functionId, Double xValue, Double yValue) {
        this.functionId = functionId;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public Double getXValue() { return xValue; }
    public void setXValue(Double xValue) { this.xValue = xValue; }
    public Double getYValue() { return yValue; }
    public void setYValue(Double yValue) { this.yValue = yValue; }
}