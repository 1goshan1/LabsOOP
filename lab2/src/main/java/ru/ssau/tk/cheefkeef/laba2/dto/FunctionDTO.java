package ru.ssau.tk.cheefkeef.laba2.dto;

public class FunctionDTO {
    private Integer id;
    private Integer userId;
    private String name;
    private String signature;

    // Конструкторы
    public FunctionDTO() {}

    public FunctionDTO(Integer id, Integer userId, String name, String signature) {
        this.id = id;
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
        return "FunctionDTO{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}