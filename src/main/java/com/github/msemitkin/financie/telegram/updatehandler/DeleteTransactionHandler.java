package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.github.msemitkin.financie.telegram.util.TransactionUtil.getTransactionRepresentation;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static java.util.Collections.emptyList;

@Component
public class DeleteTransactionHandler extends AbstractQueryHandler {
    private final TransactionService transactionService;
    private final TelegramApi telegramApi;

    public DeleteTransactionHandler(TransactionService transactionService, TelegramApi telegramApi) {
        super("transactions/delete");
        this.transactionService = transactionService;
        this.telegramApi = telegramApi;
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
        telegramApi.execute(editMessageText);
    }
}
