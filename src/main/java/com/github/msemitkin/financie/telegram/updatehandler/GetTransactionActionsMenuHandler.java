package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.JsonUtil.toJson;
import static com.github.msemitkin.financie.telegram.util.TransactionUtil.getTransactionRepresentation;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class GetTransactionActionsMenuHandler implements UpdateHandler {
    private final TelegramApi telegramApi;
    private final TransactionService transactionService;

    public GetTransactionActionsMenuHandler(TelegramApi telegramApi, TransactionService transactionService) {
        this.telegramApi = telegramApi;
        this.transactionService = transactionService;
    }

    @Override
    public boolean canHandle(Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .map(callbackData -> new Gson().fromJson(callbackData, JsonObject.class))
            .map(json -> json.get("type").getAsString())
            .map("transactions/actions"::equals)
            .orElse(false);
    }

    @Override
    public void handleUpdate(Update update) {
        Long chatId = getChatId(update);
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        JsonObject callbackJson = new Gson().fromJson(update.getCallbackQuery().getData(), JsonObject.class);
        long transactionId = callbackJson.get("transactionId").getAsLong();

        InlineKeyboardButton inlineKeyboardButton = InlineKeyboardButton.builder()
            .text("Delete")
            .callbackData(toJson(Map.of("type", "transactions/delete", "transactionId", transactionId)))
            .build();
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(inlineKeyboardButton))
            .build();
        String transaction = getTransactionRepresentation(transactionService.getTransaction(transactionId));
        String text = """
            %s
            Actions""".formatted(transaction);
        EditMessageText editMessageText = EditMessageText.builder()
            .chatId(chatId)
            .messageId(messageId)
            .text(text)
            .replyMarkup(inlineKeyboardMarkup)
            .build();
        telegramApi.execute(editMessageText);
    }
}