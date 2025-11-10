package ru.ssau.tk.cheefkeef.laba2.models;

public class User {
    private Integer id;
    private String login;
    private String role;
    private String password;
    private Boolean enabled;

    // Конструкторы
    public User() {}

    public User(String login, String role, String password, Boolean enabled) {
        this.login = login;
        this.role = role;
        this.password = password;
        this.enabled = enabled;
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

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    @Override
    public String toString() {
        return String.format("User{id=%d, login='%s', role=%s, enabled=%b}",
                id, login, role, enabled);
    }
}