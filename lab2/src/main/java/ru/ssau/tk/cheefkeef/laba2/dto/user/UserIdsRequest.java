// UserIdsRequest.java
package ru.ssau.tk.cheefkeef.laba2.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UserIdsRequest {
    @NotNull(message = "Список ID не может быть null")
    @Size(min = 1, message = "Список ID должен содержать хотя бы один элемент")
    private List<Long> ids;

    // Конструкторы, геттеры и сеттеры
    public UserIdsRequest() {}

    public UserIdsRequest(List<Long> ids) {
        this.ids = ids;
    }

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }
}