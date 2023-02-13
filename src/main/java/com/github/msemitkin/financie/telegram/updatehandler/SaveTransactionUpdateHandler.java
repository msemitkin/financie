package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.SaveTransactionCommand;
import com.github.msemitkin.financie.domain.Statistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.telegram.MessageException;
import com.github.msemitkin.financie.telegram.transaction.IncomingTransaction;
import com.github.msemitkin.financie.telegram.transaction.TransactionParser;
import com.github.msemitkin.financie.telegram.transaction.TransactionRecognizer;
import com.github.msemitkin.financie.telegram.transaction.TransactionValidator;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;
import static java.util.Objects.requireNonNull;

@Component
public class SaveTransactionUpdateHandler implements UpdateHandler {
    private final AbsSender absSender;
    private final TransactionRecognizer transactionRecognizer;
    private final TransactionValidator transactionValidator;
    private final TransactionParser transactionParser;
    private final TransactionService transactionService;
    private final StatisticsService statisticsService;

    public SaveTransactionUpdateHandler(
        AbsSender absSender,
        TransactionRecognizer transactionRecognizer,
        TransactionValidator transactionValidator,
        TransactionParser transactionParser,
        TransactionService transactionService,
        StatisticsService statisticsService
    ) {
        this.absSender = absSender;
        this.transactionRecognizer = transactionRecognizer;
        this.transactionValidator = transactionValidator;
        this.transactionParser = transactionParser;
        this.transactionService = transactionService;
        this.statisticsService = statisticsService;
    }

    @Override
    public boolean canHandle(Update update) {
        return Optional.ofNullable(update.getMessage())
            .map(Message::getText)
            .map(transactionRecognizer::hasTransactionFormat)
            .orElse(false);
    }

    @Override
    public void handleUpdate(Update update) {
        Long chatId = getChatId(update);
        String text = update.getMessage().getText();
        Long senderTelegramId = requireNonNull(getSenderTelegramId(update));
        Integer messageId = update.getMessage().getMessageId();
        try {
            transactionValidator.validateTransaction(text);

            IncomingTransaction incomingTransaction = transactionParser.parseTransaction(text);

            long userId = transactionService.getOrCreateUserByTelegramId(senderTelegramId);
            SaveTransactionCommand command = new SaveTransactionCommand(
                userId, incomingTransaction.amount(), incomingTransaction.category(), null);
            transactionService.saveTransaction(command);

            sendSuccessfullySavedTransaction(chatId, userId, messageId, incomingTransaction.category());
        } catch (MessageException e) {
            sendMessage(chatId, e.getMessage(), messageId, null);
        }
    }

    private void sendSuccessfullySavedTransaction(
        Long chatId,
        Long userId,
        Integer messageId,
        String category
    ) {
        Statistics statistics = statisticsService.getMonthlyStatistics(userId, category);
        String reply = "Saved%nTotal spend in this month: %.1f%nIn this category: %.1f"
            .formatted(statistics.total(), statistics.totalInCategory());
        sendMessage(chatId, reply, messageId, null);
    }

    private void sendMessage(
        Long chatId,
        String text,
        @Nullable Integer replyToMessageId,
        @Nullable ReplyKeyboard replyKeyboard
    ) {
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .replyToMessageId(replyToMessageId)
            .replyMarkup(replyKeyboard)
            .build();
        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
