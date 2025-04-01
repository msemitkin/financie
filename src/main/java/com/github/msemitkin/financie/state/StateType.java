package com.github.msemitkin.financie.state;

import java.util.Arrays;

public enum StateType {
    NONE(0),
    IDLE(1),
    SETTINGS(2),
    MENU(3),
    IMPORT(4),
    ADD_TRANSACTION(5),
    ENTER_TRANSACTION_CATEGORY(6);

    private final int id;

    StateType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static StateType byId(int id) {
        return Arrays.stream(values())
            .filter(type -> type.getId() == id)
            .findFirst()
            .orElseThrow();
    }
}
