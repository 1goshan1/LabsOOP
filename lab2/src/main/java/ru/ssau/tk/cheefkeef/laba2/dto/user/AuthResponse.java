// AuthResponse.java
package ru.ssau.tk.cheefkeef.laba2.dto.user;

public class AuthResponse {
    private String login;
    private String role;
    private String message;

    // Конструкторы, геттеры и сеттеры
    public AuthResponse() {}

    public AuthResponse(String login, String role, String message) {
        this.login = login;
        this.role = role;
        this.message = message;
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}