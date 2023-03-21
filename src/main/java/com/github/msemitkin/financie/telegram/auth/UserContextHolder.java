package com.github.msemitkin.financie.telegram.auth;

public class UserContextHolder {
    private UserContextHolder() {
    }

    private static final ThreadLocal<UserContext> userContext = new ThreadLocal<>();

    public static void setContext(UserContext context) {
        userContext.set(context);
    }

    public static UserContext getContext() {
        return userContext.get();
    }

    public static void clearContext() {
        userContext.remove();
    }
}
