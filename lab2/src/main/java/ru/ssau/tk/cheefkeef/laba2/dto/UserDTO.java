package ru.ssau.tk.cheefkeef.laba2.dto;

public class UserDTO {
    private Integer id;
    private String login;
    private String role;
    private String password;
    private Boolean enabled;

    // Конструкторы
    public UserDTO() {}

    public UserDTO(Integer id, String login, String role, Boolean enabled) {
        this.id = id;
        this.login = login;
        this.role = role;
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
        return "UserDTO{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}