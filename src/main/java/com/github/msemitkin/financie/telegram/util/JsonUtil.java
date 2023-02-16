package com.github.msemitkin.financie.telegram.util;

import com.google.gson.JsonObject;

import java.util.Map;

public class JsonUtil {
    private JsonUtil() {
    }

    public static String toJson(Map<String, Object> values) {
        JsonObject jsonObject = new JsonObject();
        values.forEach((key, value) -> jsonObject.addProperty(key, value.toString()));
        return jsonObject.toString();
    }
}
