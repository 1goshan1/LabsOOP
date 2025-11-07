package ru.ssau.tk.cheefkeef.laba2.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String login;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "role_enum")
    private Role role = Role.USER;

    @Column(nullable = false)
    private String password;

    public User() {}

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}