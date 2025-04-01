package com.github.msemitkin.financie.mono;

public class WebhookEvent {
    private String type;
    private AccountData data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AccountData getData() {
        return data;
    }

    public void setData(AccountData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "WebhookEvent{" +
               "type='" + type + '\'' +
               ", data=" + data +
               '}';
    }
}
