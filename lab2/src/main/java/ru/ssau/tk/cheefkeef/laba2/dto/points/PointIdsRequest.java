// PointIdsRequest.java
package ru.ssau.tk.cheefkeef.laba2.dto.points;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class PointIdsRequest {
    @NotNull(message = "Список ID не может быть null")
    @Size(min = 1, message = "Список ID должен содержать хотя бы один элемент")
    private List<Long> ids;

    // Конструкторы, геттеры и сеттеры
    public PointIdsRequest() {}

    public PointIdsRequest(List<Long> ids) {
        this.ids = ids;
    }

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }
}