package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.callback.Callback;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.DeleteTransactionCommand;
import com.github.msemitkin.financie.telegram.callback.command.GetTransactionActionsCommand;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractQueryHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.UUID;

import static com.github.msemitkin.financie.telegram.callback.CallbackType.DELETE_TRANSACTION;
import static com.github.msemitkin.financie.telegram.util.TransactionUtil.getTransactionRepresentation;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class GetTransactionActionsMenuHandler extends AbstractQueryHandler {
    private final TelegramApi telegramApi;
    private final TransactionService transactionService;

    public GetTransactionActionsMenuHandler(
        TelegramApi telegramApi,
        TransactionService transactionService,
        CallbackService callbackService
    ) {
        super(CallbackType.GET_TRANSACTION_ACTIONS, callbackService);
        this.telegramApi = telegramApi;
        this.transactionService = transactionService;
    }

    @Override
    public void handleUpdate(Update update) {
        Long chatId = getChatId(update);
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        var callbackData = getCallbackData(update, GetTransactionActionsCommand.class);
        long transactionId = callbackData.transactionId();

        var callback = new Callback<>(DELETE_TRANSACTION, new DeleteTransactionCommand(transactionId));
        UUID callbackId = callbackService.saveCallback(callback);

        InlineKeyboardButton inlineKeyboardButton = InlineKeyboardButton.builder()
            .text(ResourceService.getValue("button.delete", UserContextHolder.getContext().locale()))
            .callbackData(callbackId.toString())
            .build();
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(inlineKeyboardButton))
            .build();
        String transaction = getTransactionRepresentation(transactionService.getTransaction(transactionId));

        String text = transaction.concat("\n")
            .concat(ResourceService.getValue("transaction-actions", UserContextHolder.getContext().locale()));
        EditMessageText editMessageText = EditMessageText.builder()
            .chatId(chatId)
            .messageId(messageId)
            .text(text)
            .replyMarkup(inlineKeyboardMarkup)
            .build();
        telegramApi.execute(editMessageText);
    }
}
