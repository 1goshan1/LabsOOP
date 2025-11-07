package ru.ssau.tk.cheefkeef.laba2.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Functions")
public class Functions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "u_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String signature;

    // Конструкторы
    public Functions() {}

    public Functions(Long userId, String name, String signature) {
        this.userId = userId;
        this.name = name;
        this.signature = signature;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}