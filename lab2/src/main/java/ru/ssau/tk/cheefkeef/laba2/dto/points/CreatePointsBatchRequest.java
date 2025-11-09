// CreatePointsBatchRequest.java
package ru.ssau.tk.cheefkeef.laba2.dto.points;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CreatePointsBatchRequest {
    @NotNull(message = "ID функции не может быть null")
    @Min(value = 1, message = "ID функции должен быть не менее 1")
    private Long functionId;

    @NotNull(message = "Список точек не может быть null")
    @Size(min = 1, message = "Список точек должен содержать хотя бы одну точку")
    private List<PointCoordinate> points;

    // Конструкторы, геттеры и сеттеры
    public CreatePointsBatchRequest() {}

    public CreatePointsBatchRequest(Long functionId, List<PointCoordinate> points) {
        this.functionId = functionId;
        this.points = points;
    }

    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public List<PointCoordinate> getPoints() { return points; }
    public void setPoints(List<PointCoordinate> points) { this.points = points; }
}