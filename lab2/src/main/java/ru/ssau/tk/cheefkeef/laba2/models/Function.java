package ru.ssau.tk.cheefkeef.laba2.models;

public class Function {
    private Integer id;
    private Integer userId;
    private String name;
    private String signature;

    // Конструкторы
    public Function() {}

    public Function(Integer userId, String name, String signature) {
        this.userId = userId;
        this.name = name;
        this.signature = signature;
    }

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    @Override
    public String toString() {
        return String.format("Function{id=%d, userId=%d, name='%s', signature='%s'}",
                id, userId, name, signature);
    }
}