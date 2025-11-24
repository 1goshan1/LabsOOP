// UpdatePointsBatchRequest.java
package ru.ssau.tk.cheefkeef.laba2.dto.points;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class UpdatePointsBatchRequest {
    @NotNull(message = "ID функции не может быть пустым")
    private Long functionId;

    @Valid
    private List<UpdatePointCoordinate> points;

    // Конструкторы, геттеры и сеттеры
    public UpdatePointsBatchRequest() {}

    public UpdatePointsBatchRequest(Long functionId, List<UpdatePointCoordinate> points) {
        this.functionId = functionId;
        this.points = points;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public List<UpdatePointCoordinate> getPoints() {
        return points;
    }

    public void setPoints(List<UpdatePointCoordinate> points) {
        this.points = points;
    }
}