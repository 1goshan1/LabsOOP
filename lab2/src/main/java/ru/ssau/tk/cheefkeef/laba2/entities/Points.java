package ru.ssau.tk.cheefkeef.laba2.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Points")
public class Points {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "f_id", nullable = false)
    private Long functionId;

    @Column(name = "x_value", nullable = false)
    private Double x;

    @Column(name = "y_value", nullable = false)
    private Double y;

    // Конструкторы
    public Points() {}

    public Points(Long functionId, Double x, Double y) {
        this.functionId = functionId;
        this.x = x;
        this.y = y;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }
    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }
}