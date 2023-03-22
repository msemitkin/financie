package com.github.msemitkin.financie.telegram.keyboard;

import com.github.msemitkin.financie.resources.ResourceService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.Locale;

@Service
public class KeyboardService {

    public ReplyKeyboardMarkup getDefaultReplyMarkup(Locale locale) {
        return ReplyKeyboardMarkup.builder()
            .keyboardRow(new KeyboardRow(List.of(
                KeyboardButton.builder()
                    .text(ResourceService.getValue("button.today", locale))
                    .build(),
                KeyboardButton.builder()
                    .text(ResourceService.getValue("button.this-month", locale))
                    .build()
            )))
            .resizeKeyboard(true)
            .build();
    }
}
