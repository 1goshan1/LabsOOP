// ChangePasswordRequest.java
package ru.ssau.tk.cheefkeef.laba2.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
    @NotBlank(message = "Старый пароль не может быть пустым")
    private String oldPassword;

    @NotBlank(message = "Новый пароль не может быть пустым")
    @Size(min = 6, message = "Новый пароль должен содержать минимум 6 символов")
    private String newPassword;

    // Конструкторы, геттеры и сеттеры
    public ChangePasswordRequest() {}

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}