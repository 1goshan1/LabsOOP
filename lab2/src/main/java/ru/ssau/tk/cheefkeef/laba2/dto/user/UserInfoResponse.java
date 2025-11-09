// UserInfoResponse.java
package ru.ssau.tk.cheefkeef.laba2.dto.user;

public class UserInfoResponse {
    private Long id;
    private String login;
    private String role;

    // Конструкторы, геттеры и сеттеры
    public UserInfoResponse() {}

    public UserInfoResponse(Long id, String login, String role) {
        this.id = id;
        this.login = login;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}