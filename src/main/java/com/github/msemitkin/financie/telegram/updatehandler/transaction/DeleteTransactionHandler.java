package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.callback.CallbackDataExtractor;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.DeleteTransactionCommand;
import com.github.msemitkin.financie.telegram.updatehandler.BaseUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.github.msemitkin.financie.telegram.util.TransactionUtil.getTransactionRepresentation;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static java.util.Collections.emptyList;

@Component
public class DeleteTransactionHandler extends BaseUpdateHandler {
    private final TransactionService transactionService;
    private final TelegramApi telegramApi;
    private final CallbackDataExtractor callbackDataExtractor;

    public DeleteTransactionHandler(
        TransactionService transactionService,
        CallbackService callbackService,
        TelegramApi telegramApi,
        CallbackDataExtractor callbackDataExtractor) {
        super(UpdateMatcher.callbackQueryMatcher(callbackService, CallbackType.DELETE_TRANSACTION));
        this.transactionService = transactionService;
        this.telegramApi = telegramApi;
        this.callbackDataExtractor = callbackDataExtractor;
    }

    @Override
    protected void handleUpdate(Update update) {
        var callbackData = callbackDataExtractor.getCallbackData(update, DeleteTransactionCommand.class);
        Long chatId = getChatId(update);
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        long transactionId = callbackData.transactionId();
        Transaction transaction = transactionService.getTransaction(transactionId);
        transactionService.deleteTransaction(transactionId);

        String text = ResourceService.getValue("transaction-deleted-reply", UserContextHolder.getContext().locale())
            .concat("\n").concat(getTransactionRepresentation(transaction));
        EditMessageText editMessageText = EditMessageText.builder()
            .chatId(chatId)
            .messageId(messageId)
            .text(text)
            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(emptyList()).build())
            .build();
        telegramApi.execute(editMessageText);
    }
}
