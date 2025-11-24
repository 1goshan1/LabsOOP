// TabulatedPointDTO.java
package ru.ssau.tk.cheefkeef.laba2.dto.points;

public class TabulatedPointDTO {
    private double xValue;
    private double yValue;

    public TabulatedPointDTO() {}

    public TabulatedPointDTO(double xValue, double yValue) {
        this.xValue = xValue;
        this.yValue = yValue;
    }

    // Геттеры и сеттеры
    public double getXValue() {
        return xValue;
    }

    public void setXValue(double xValue) {
        this.xValue = xValue;
    }

    public double getYValue() {
        return yValue;
    }

    public void setYValue(double yValue) {
        this.yValue = yValue;
    }
}