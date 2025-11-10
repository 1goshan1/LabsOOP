package ru.ssau.tk.cheefkeef.laba2.dto;

import java.util.List;

public class UserIdsRequest {
    private List<Integer> ids;

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }
}