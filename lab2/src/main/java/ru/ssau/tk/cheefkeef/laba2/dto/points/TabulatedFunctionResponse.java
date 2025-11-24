// TabulatedFunctionResponse.java
package ru.ssau.tk.cheefkeef.laba2.dto.points;

import java.util.List;

public class TabulatedFunctionResponse {
    private List<TabulatedPointDTO> points;

    public TabulatedFunctionResponse() {}

    public TabulatedFunctionResponse(List<TabulatedPointDTO> points) {
        this.points = points;
    }

    // Геттеры и сеттеры
    public List<TabulatedPointDTO> getPoints() {
        return points;
    }

    public void setPoints(List<TabulatedPointDTO> points) {
        this.points = points;
    }
}