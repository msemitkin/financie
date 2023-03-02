package com.github.msemitkin.financie.telegram.updatehandler.categories;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public record Response(String text, InlineKeyboardMarkup keyboardMarkup) {
}
