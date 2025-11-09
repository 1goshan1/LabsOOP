// UpdateUserRequest.java
package ru.ssau.tk.cheefkeef.laba2.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {
    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 1, message = "Логин должен содержать хотя бы 1 символ")
    private String login;

    @NotBlank(message = "Роль не может быть пустой")
    @Size(min = 1, message = "Роль должна содержать хотя бы 1 символ")
    private String role;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 1, message = "Пароль должен содержать хотя бы 1 символ")
    private String password;

    // Конструкторы, геттеры и сеттеры
    public UpdateUserRequest() {}

    public UpdateUserRequest(String login, String role, String password) {
        this.login = login;
        this.role = role;
        this.password = password;
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}