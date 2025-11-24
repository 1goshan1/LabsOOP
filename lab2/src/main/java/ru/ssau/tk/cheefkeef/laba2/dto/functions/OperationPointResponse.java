package ru.ssau.tk.cheefkeef.laba2.dto.functions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OperationPointResponse {
    private Long id;
    @JsonProperty("functionId")
    private Long functionId;
    @JsonProperty("xvalue")
    private Double xvalue;
    @JsonProperty("yvalue")
    private Double yvalue;

    public OperationPointResponse(Long id, Long functionId, Double xvalue, Double yvalue) {
        this.id = id;
        this.functionId = functionId;
        this.xvalue = xvalue;
        this.yvalue = yvalue;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public Double getXvalue() {
        return xvalue;
    }

    public Double getYvalue() {
        return yvalue;
    }
}