package ru.ssau.tk.cheefkeef.laba2.models;

public class User {
    private Integer id;
    private String login;
    private String role;
    private String password;

    // Конструкторы
    public User() {}

    public User(String login, String role, String password) {
        this.login = login;
        this.role = role;
        this.password = password;
    }

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return String.format("User{id=%d, login='%s', role=%s, password='%s'}",
                id, login, role, password != null ? "***" : "null");
    }
}