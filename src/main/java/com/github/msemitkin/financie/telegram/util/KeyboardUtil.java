package com.github.msemitkin.financie.telegram.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;

public class KeyboardUtil {
    private KeyboardUtil() {
    }

    public static KeyboardButton requestLocationButton(String text) {
        return KeyboardButton.builder()
            .text(text)
            .requestLocation(true)
            .build();
    }

    public static KeyboardButton textButton(String text) {
        return KeyboardButton.builder()
            .text(text)
            .build();
    }

    public static ReplyKeyboardMarkup keyboard(KeyboardRow... rows) {
        var builder = ReplyKeyboardMarkup.builder()
            .resizeKeyboard(true);
        for (KeyboardRow row : rows) {
            builder.keyboardRow(row);
        }
        return builder.build();
    }

    public static KeyboardRow row(KeyboardButton... buttons) {
        return new KeyboardRow(Arrays.stream(buttons).toList());
    }
}
