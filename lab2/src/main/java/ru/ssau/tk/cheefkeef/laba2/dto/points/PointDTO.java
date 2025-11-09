// PointDTO.java
package ru.ssau.tk.cheefkeef.laba2.dto.points;

public class PointDTO {
    private Long id;
    private Long functionId;
    private Double xValue;
    private Double yValue;

    // Конструкторы, геттеры и сеттеры
    public PointDTO() {}

    public PointDTO(Long id, Long functionId, Double xValue, Double yValue) {
        this.id = id;
        this.functionId = functionId;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public Double getXValue() { return xValue; }
    public void setXValue(Double xValue) { this.xValue = xValue; }
    public Double getYValue() { return yValue; }
    public void setYValue(Double yValue) { this.yValue = yValue; }
}