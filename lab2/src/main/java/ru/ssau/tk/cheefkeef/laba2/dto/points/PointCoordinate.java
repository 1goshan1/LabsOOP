// PointCoordinate.java
package ru.ssau.tk.cheefkeef.laba2.dto.points;

import jakarta.validation.constraints.NotNull;

public class PointCoordinate {
    @NotNull(message = "Координата X не может быть null")
    private Double xValue;

    @NotNull(message = "Координата Y не может быть null")
    private Double yValue;

    // Конструкторы, геттеры и сеттеры
    public PointCoordinate() {}

    public PointCoordinate(Double xValue, Double yValue) {
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public Double getXValue() { return xValue; }
    public void setXValue(Double xValue) { this.xValue = xValue; }
    public Double getYValue() { return yValue; }
    public void setYValue(Double yValue) { this.yValue = yValue; }
}