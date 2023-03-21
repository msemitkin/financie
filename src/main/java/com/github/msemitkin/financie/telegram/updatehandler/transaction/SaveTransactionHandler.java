package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.SaveTransactionCommand;
import com.github.msemitkin.financie.domain.Statistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.MessageException;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.transaction.IncomingTransaction;
import com.github.msemitkin.financie.telegram.transaction.TransactionCommandValidator;
import com.github.msemitkin.financie.telegram.transaction.TransactionParser;
import com.github.msemitkin.financie.telegram.transaction.TransactionRecognizer;
import com.github.msemitkin.financie.telegram.updatehandler.UpdateHandler;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;

@Component
public class SaveTransactionHandler implements UpdateHandler {
    private final TelegramApi telegramApi;
    private final TransactionRecognizer transactionRecognizer;
    private final TransactionCommandValidator transactionCommandValidator;
    private final TransactionParser transactionParser;
    private final TransactionService transactionService;
    private final StatisticsService statisticsService;
    private final UserService userService;

    public SaveTransactionHandler(
        TelegramApi telegramApi,
        TransactionRecognizer transactionRecognizer,
        TransactionCommandValidator transactionCommandValidator,
        TransactionParser transactionParser,
        TransactionService transactionService,
        StatisticsService statisticsService,
        UserService userService
    ) {
        this.telegramApi = telegramApi;
        this.transactionRecognizer = transactionRecognizer;
        this.transactionCommandValidator = transactionCommandValidator;
        this.transactionParser = transactionParser;
        this.transactionService = transactionService;
        this.statisticsService = statisticsService;
        this.userService = userService;
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
        long senderTelegramId = getSenderTelegramId(update);
        Integer messageId = update.getMessage().getMessageId();
        try {
            transactionCommandValidator.validateTransaction(text);

            IncomingTransaction incomingTransaction = transactionParser.parseTransaction(text);

            long userId = userService.getUserByTelegramId(senderTelegramId).id();
            SaveTransactionCommand command = new SaveTransactionCommand(
                userId, incomingTransaction.amount(), incomingTransaction.category(), null, null);
            transactionService.saveTransaction(command);

            sendSuccessfullySavedTransaction(chatId, userId, messageId, incomingTransaction.category());
        } catch (MessageException e) {
            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .text(e.getMessage())
                .replyToMessageId(messageId)
                .build());
        }
    }

    private void sendSuccessfullySavedTransaction(
        Long chatId,
        Long userId,
        Integer messageId,
        String category
    ) {
        Statistics dailyStatistics = statisticsService.getDailyStatistics(userId, category, LocalDate.now());
        Statistics monthlyStatistics = statisticsService
            .getStatistics(userId, category, YearMonth.now().atDay(1).atStartOfDay(), LocalDateTime.now());
        String reply = StringSubstitutor.replace(
            ResourceService.getValue("transaction-saved-reply", UserContextHolder.getContext().locale()),
            Map.of(
                "today", formatNumber(dailyStatistics.total()),
                "this_month", formatNumber(monthlyStatistics.total()),
                "this_month_in_category", formatNumber(monthlyStatistics.totalInCategory())
            )
        );
        telegramApi.execute(SendMessage.builder()
            .chatId(chatId)
            .text(reply)
            .parseMode(ParseMode.MARKDOWNV2)
            .replyToMessageId(messageId)
            .build());
    }
}
