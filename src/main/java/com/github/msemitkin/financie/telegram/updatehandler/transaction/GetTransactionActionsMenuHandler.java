package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.ResponseSender;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.callback.Callback;
import com.github.msemitkin.financie.telegram.callback.CallbackDataExtractor;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.DeleteTransactionCommand;
import com.github.msemitkin.financie.telegram.callback.command.GetTransactionActionsCommand;
import com.github.msemitkin.financie.telegram.updatehandler.BaseUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.UUID;

import static com.github.msemitkin.financie.telegram.callback.CallbackType.DELETE_TRANSACTION;
import static com.github.msemitkin.financie.telegram.util.TransactionUtil.getTransactionRepresentation;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getAccessibleMessageId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class GetTransactionActionsMenuHandler extends BaseUpdateHandler {
    private final ResponseSender responseSender;
    private final TransactionService transactionService;
    private final CallbackDataExtractor callbackDataExtractor;
    private final CallbackService callbackService;

    public GetTransactionActionsMenuHandler(
        ResponseSender responseSender,
        TransactionService transactionService,
        CallbackService callbackService,
        CallbackDataExtractor callbackDataExtractor
    ) {
        super(UpdateMatcher.callbackQueryMatcher(callbackService, CallbackType.GET_TRANSACTION_ACTIONS));
        this.responseSender = responseSender;
        this.transactionService = transactionService;
        this.callbackDataExtractor = callbackDataExtractor;
        this.callbackService = callbackService;
    }

    @Override
    public void handleUpdate(Update update) {
        Long chatId = getChatId(update);
        Integer messageId = getAccessibleMessageId(update.getCallbackQuery().getMessage());
        var callbackData = callbackDataExtractor.getCallbackData(update, GetTransactionActionsCommand.class);
        long transactionId = callbackData.transactionId();

        var callback = new Callback<>(DELETE_TRANSACTION, new DeleteTransactionCommand(transactionId));
        UUID callbackId = callbackService.saveCallback(callback);

        InlineKeyboardButton inlineKeyboardButton = InlineKeyboardButton.builder()
            .text(ResourceService.getValue("button.delete", UserContextHolder.getContext().locale()))
            .callbackData(callbackId.toString())
            .build();
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
            .keyboardRow(new InlineKeyboardRow(inlineKeyboardButton))
            .build();
        String transaction = getTransactionRepresentation(transactionService.getTransaction(transactionId));

        String text = transaction.concat("\n")
            .concat(ResourceService.getValue("transaction-actions", UserContextHolder.getContext().locale()));
        responseSender.sendResponse(chatId, messageId, inlineKeyboardMarkup, text, false);
    }
}
