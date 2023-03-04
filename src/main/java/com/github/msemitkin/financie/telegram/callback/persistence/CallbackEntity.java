package com.github.msemitkin.financie.telegram.callback.persistence;

import com.github.msemitkin.financie.telegram.callback.CallbackType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "callback")
public class CallbackEntity {
    @Id
    @Column(name = "id")
    private UUID id;
    @Column(name = "type")
    private CallbackType type;
    @Column(name = "payload", columnDefinition = "json")
    private String payload;

    public CallbackEntity() {
    }

    public CallbackEntity(UUID id, CallbackType type, String payload) {
        this.id = id;
        this.type = type;
        this.payload = payload;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CallbackType getType() {
        return type;
    }

    public void setType(CallbackType type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
