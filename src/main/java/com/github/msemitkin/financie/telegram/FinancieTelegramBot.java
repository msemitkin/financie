package com.github.msemitkin.financie.telegram;

import com.github.msemitkin.financie.domain.CategoryStatistics;
import com.github.msemitkin.financie.domain.SaveTransactionCommand;
import com.github.msemitkin.financie.domain.Statistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.telegram.transaction.IncomingTransaction;
import com.github.msemitkin.financie.telegram.transaction.TransactionParser;
import com.github.msemitkin.financie.telegram.transaction.TransactionRecognizer;
import com.github.msemitkin.financie.telegram.transaction.TransactionValidator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.github.msemitkin.financie.telegram.command.BotCommand.MONTHLY_STATISTICS;
import static com.github.msemitkin.financie.telegram.command.BotCommand.START;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatDate;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatMonth;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;
import static java.util.Objects.requireNonNull;

@Component
public class FinancieTelegramBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(FinancieTelegramBot.class);

    private final String username;
    private final TransactionService transactionService;
    private final StatisticsService statisticsService;
    private final TransactionParser transactionParser;
    private final TransactionValidator transactionValidator;
    private final TransactionRecognizer transactionRecognizer;

    public FinancieTelegramBot(
        @Value("${bot.telegram.username}") String username,
        @Value("${bot.telegram.token}") String botToken,
        TransactionService transactionService,
        StatisticsService statisticsService,
        TransactionParser transactionParser,
        TransactionValidator transactionValidator,
        TransactionRecognizer transactionRecognizer) {
        super(botToken);
        this.username = username;
        this.transactionService = transactionService;
        this.statisticsService = statisticsService;
        this.transactionParser = transactionParser;
        this.transactionValidator = transactionValidator;
        this.transactionRecognizer = transactionRecognizer;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onRegister() {
        logger.info("Bot successfully registered");
        super.onRegister();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        } else {
            logger.warn("Got unrecognized update {}", update);
        }
    }

    private void handleMessage(Update update) {
        Message message = update.getMessage();
        logger.info("Received message");
        if (message.hasText()) {
            String messageText = message.getText();
            Long chatId = getChatId(update);
            long senderTelegramId = requireNonNull(getSenderTelegramId(update));

            if (START.getCommand().equals(messageText)) {
                sendWelcomeMessage(chatId);
            } else {
                if (MONTHLY_STATISTICS.getCommand().equals(messageText)) {
                    handleGetMonthlyStatisticsRequest(chatId, senderTelegramId);
                } else if (transactionRecognizer.hasTransactionFormat(messageText)) {
                    try {
                        transactionValidator.validateTransaction(messageText);

                        IncomingTransaction incomingTransaction = transactionParser.parseTransaction(messageText);

                        long userId = transactionService.getOrCreateUserByTelegramId(senderTelegramId);
                        SaveTransactionCommand command = new SaveTransactionCommand(
                            userId, incomingTransaction.amount(), incomingTransaction.category(), null);
                        transactionService.saveTransaction(command);

                        sendSuccessfullySavedTransaction(chatId, userId, message.getMessageId(), incomingTransaction.category());
                    } catch (MessageException e) {
                        sendMessage(chatId, e.getMessage(), message.getMessageId(), null);
                    }
                }
            }
        }
    }

    private void handleGetMonthlyStatisticsRequest(Long chatId, Long userTelegramId) {
        long userId = transactionService.getOrCreateUserByTelegramId(userTelegramId);
        List<CategoryStatistics> statistics = statisticsService
            .getMonthlyStatistics(userId);

        if (statistics.isEmpty()) {
            String text = "No transactions in " + formatMonth(LocalDate.now().getMonth());
            sendMessage(chatId, text, null, null);
            return;
        }

        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder = InlineKeyboardMarkup.builder();
        statistics.forEach(stats -> {
            String text = "%.1f: %s".formatted(stats.amount(), stats.category());
            String callbackData = toJsonFormat(Map.of("type", "monthlystats", "category", stats.category()));
            keyboardBuilder.keyboardRow(List.of(inlineButton(text, callbackData)));
        });
        InlineKeyboardMarkup keyboard = keyboardBuilder.build();
        sendMessage(chatId, LocalDate.now().getMonth().toString().concat(" statistics"), null, keyboard);
    }

    private void sendWelcomeMessage(Long chatIt) {
        ReplyKeyboardMarkup markup = ReplyKeyboardMarkup.builder()
            .keyboardRow(new KeyboardRow(List.of(button("Monthly statistics"))))
            .build();
        sendMessage(chatIt, "Welcome", null, markup);
    }

    private void handleCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        JsonObject jsonObject = new Gson().fromJson(data, JsonObject.class);
        if ("monthlystats".equals(jsonObject.get("type").getAsString())) {
            String category = jsonObject.get("category").getAsString();
            Long telegramUserId = requireNonNull(getSenderTelegramId(update));
            long userId = transactionService.getOrCreateUserByTelegramId(telegramUserId);
            List<Transaction> transactionsInCategory = statisticsService
                .getMonthlyCategoryStatistics(userId, category);

            String header = LocalDate.now().getMonth().toString().concat(" statistics");
            String message = transactionsInCategory.stream()
                .reduce(new StringBuilder(header),
                    (builder, curr) -> {
                        String text = "%n%s : %.1f".formatted(formatDate(curr.time().toLocalDate()), curr.amount());
                        return builder.append(text);
                    }, StringBuilder::append)
                .toString();
            sendMessage(getChatId(update), message, null, null);
        } else {
            logger.warn("Unrecognized callback: {}", callbackQuery.getData());
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
        try {
            SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyToMessageId(replyToMessageId)
                .replyMarkup(replyKeyboard)
                .build();
            this.execute(sendMessage);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String toJsonFormat(Map<String, String> values) {
        JsonObject jsonObject = new JsonObject();
        values.forEach(jsonObject::addProperty);
        return jsonObject.toString();
    }

    private InlineKeyboardButton inlineButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
            .text(text)
            .callbackData(callbackData)
            .build();
    }

    private KeyboardButton button(String text) {
        return KeyboardButton.builder()
            .text(text)
            .build();
    }
}
