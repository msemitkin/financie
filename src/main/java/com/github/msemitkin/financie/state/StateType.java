package com.github.msemitkin.financie.state;

public enum StateType {
    NONE(0),
    IDLE(1),
    SETTINGS(2),
    MENU(3),
    IMPORT(4);

    private final int id;

    StateType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
