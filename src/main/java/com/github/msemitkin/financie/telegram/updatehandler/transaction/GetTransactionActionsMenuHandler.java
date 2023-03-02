package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractQueryHandler;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Map;

import static com.github.msemitkin.financie.telegram.util.JsonUtil.toJson;
import static com.github.msemitkin.financie.telegram.util.TransactionUtil.getTransactionRepresentation;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class GetTransactionActionsMenuHandler extends AbstractQueryHandler {
    private final TelegramApi telegramApi;
    private final TransactionService transactionService;

    public GetTransactionActionsMenuHandler(TelegramApi telegramApi, TransactionService transactionService) {
        super("transactions/actions");
        this.telegramApi = telegramApi;
        this.transactionService = transactionService;
    }

    @Override
    public void handleUpdate(Update update) {
        Long chatId = getChatId(update);
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        JsonObject callbackJson = getCallbackData(update);
        long transactionId = callbackJson.get("transactionId").getAsLong();

        InlineKeyboardButton inlineKeyboardButton = InlineKeyboardButton.builder()
            .text("Delete")
            .callbackData(toJson(Map.of("tp", "transactions/delete", "transactionId", transactionId)))
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
