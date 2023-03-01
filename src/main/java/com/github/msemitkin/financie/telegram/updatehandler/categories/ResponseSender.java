package com.github.msemitkin.financie.telegram.updatehandler.categories;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

interface ResponseSender {
    void sendResponse(String text, InlineKeyboardMarkup keyboardMarkup);
}
