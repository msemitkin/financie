package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.TransactionUtil.getTransactionRepresentation;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static java.util.Collections.emptyList;

@Component
public class DeleteTransactionUpdateHandler implements UpdateHandler {
    private final TransactionService transactionService;
    private final AbsSender absSender;

    public DeleteTransactionUpdateHandler(TransactionService transactionService, AbsSender absSender) {
        this.transactionService = transactionService;
        this.absSender = absSender;
    }

    @Override
    public boolean canHandle(Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .map(data -> new Gson().fromJson(data, JsonObject.class))
            .map(json -> json.get("type").getAsString())
            .map("transactions/delete"::equals)
            .orElse(false);
    }

    @Override
    public void handleUpdate(Update update) {
        JsonObject callbackJson = new Gson().fromJson(update.getCallbackQuery().getData(), JsonObject.class);
        Long chatId = getChatId(update);
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        long transactionId = callbackJson.get("transactionId").getAsLong();
        Transaction transaction = transactionService.getTransaction(transactionId);
        transactionService.deleteTransaction(transactionId);

        EditMessageText editMessageText = EditMessageText.builder()
            .chatId(chatId)
            .messageId(messageId)
            .text("Deleted transaction%n%s".formatted(getTransactionRepresentation(transaction)))
            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(emptyList()).build())
            .build();
        try {
            absSender.execute(editMessageText);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
