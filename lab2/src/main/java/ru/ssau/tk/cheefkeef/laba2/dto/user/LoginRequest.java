// LoginRequest.java
package ru.ssau.tk.cheefkeef.laba2.dto.user;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Логин не может быть пустым")
    private String login;

    @NotBlank(message = "Пароль не может быть пустым")
    private String password;

    // Конструкторы, геттеры и сеттеры
    public LoginRequest() {}

    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}