// UpdatePointCoordinate.java
package ru.ssau.tk.cheefkeef.laba2.dto.points;

import jakarta.validation.constraints.NotNull;

public class UpdatePointCoordinate {
    @NotNull(message = "ID точки не может быть пустым")
    private Long id;

    @NotNull(message = "Значение X не может быть пустым")
    private Double xValue;

    @NotNull(message = "Значение Y не может быть пустым")
    private Double yValue;

    // Конструкторы, геттеры и сеттеры
    public UpdatePointCoordinate() {}

    public UpdatePointCoordinate(Long id, Double xValue, Double yValue) {
        this.id = id;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getXValue() {
        return xValue;
    }

    public void setXValue(Double xValue) {
        this.xValue = xValue;
    }

    public Double getYValue() {
        return yValue;
    }

    public void setYValue(Double yValue) {
        this.yValue = yValue;
    }
}