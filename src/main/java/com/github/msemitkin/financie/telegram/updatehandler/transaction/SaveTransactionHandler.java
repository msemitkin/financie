package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.SaveTransactionCommand;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.telegram.MessageException;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContext;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.transaction.IncomingTransaction;
import com.github.msemitkin.financie.telegram.transaction.TransactionCommandValidator;
import com.github.msemitkin.financie.telegram.transaction.TransactionParser;
import com.github.msemitkin.financie.telegram.transaction.TransactionRecognizer;
import com.github.msemitkin.financie.telegram.updatehandler.chain.UpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.common.SendSuccessfullySavedTransactionService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;

@Component
public class SaveTransactionHandler extends UpdateHandler {
    private final TelegramApi telegramApi;
    private final TransactionRecognizer transactionRecognizer;
    private final TransactionCommandValidator transactionCommandValidator;
    private final TransactionParser transactionParser;
    private final TransactionService transactionService;
    private final UserService userService;
    private final SendSuccessfullySavedTransactionService sendSuccessfullySavedTransactionService;

    public SaveTransactionHandler(
        TelegramApi telegramApi,
        TransactionRecognizer transactionRecognizer,
        TransactionCommandValidator transactionCommandValidator,
        TransactionParser transactionParser,
        TransactionService transactionService,
        UserService userService,
        SendSuccessfullySavedTransactionService sendSuccessfullySavedTransactionService
    ) {
        this.telegramApi = telegramApi;
        this.transactionRecognizer = transactionRecognizer;
        this.transactionCommandValidator = transactionCommandValidator;
        this.transactionParser = transactionParser;
        this.transactionService = transactionService;
        this.userService = userService;
        this.sendSuccessfullySavedTransactionService = sendSuccessfullySavedTransactionService;
    }

    @Override
    protected boolean canHandle(Update update) {
        return Optional.ofNullable(update.getMessage())
            .map(Message::getText)
            .map(transactionRecognizer::hasTransactionFormat)
            .orElse(false);
    }

    @Override
    protected void handleUpdate(Update update) {
        UserContext userContext = UserContextHolder.getContext();
        Locale locale = userContext.locale();
        ZoneId timeZoneId = userContext.timeZone().toZoneId();

        Long chatId = getChatId(update);
        String text = update.getMessage().getText();
        long senderTelegramId = getSenderTelegramId(update);
        Integer messageId = update.getMessage().getMessageId();
        try {
            transactionCommandValidator.validateTransaction(text);

            IncomingTransaction incomingTransaction = transactionParser.parseTransaction(text);

            long userId = userService.getUserByTelegramId(senderTelegramId).id();
            SaveTransactionCommand command = new SaveTransactionCommand(
                userId, incomingTransaction.amount(), incomingTransaction.category(), null, null);
            transactionService.saveTransaction(command);

            sendSuccessfullySavedTransactionService
                .sendSuccessfullySavedTransaction(chatId, userId, messageId, incomingTransaction.category(), locale, timeZoneId);
        } catch (MessageException e) {
            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .text(e.getMessage())
                .replyToMessageId(messageId)
                .build());
        }
    }
}
