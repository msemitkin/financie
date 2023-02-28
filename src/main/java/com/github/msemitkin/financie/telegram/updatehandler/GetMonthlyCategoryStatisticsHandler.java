package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatMonth;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.telegram.util.JsonUtil.toJson;
import static com.github.msemitkin.financie.telegram.util.MarkdownUtil.escapeMarkdownV2;
import static com.github.msemitkin.financie.telegram.util.TransactionUtil.getTransactionRepresentation;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;
import static java.util.Objects.requireNonNull;

@Component
public class GetMonthlyCategoryStatisticsHandler extends AbstractQueryHandler {
    private final TransactionService transactionService;
    private final StatisticsService statisticsService;
    private final TelegramApi telegramApi;
    private final int maxNumberOfStatisticsRecords;

    public GetMonthlyCategoryStatisticsHandler(
        TransactionService transactionService,
        StatisticsService statisticsService,
        TelegramApi telegramApi,
        @Value("${com.github.msemitkin.financie.statistics.max-number-of-displayed-records}")
        int maxNumberOfStatisticsRecords
    ) {
        super("monthly_stats");
        this.transactionService = transactionService;
        this.statisticsService = statisticsService;
        this.telegramApi = telegramApi;
        this.maxNumberOfStatisticsRecords = maxNumberOfStatisticsRecords;
    }

    @Override
    public void handleUpdate(Update update) {
        JsonObject jsonObject = new Gson().fromJson(update.getCallbackQuery().getData(), JsonObject.class);
        String category = jsonObject.get("category").getAsString();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long telegramUserId = requireNonNull(getSenderTelegramId(update));
        long userId = transactionService.getOrCreateUserByTelegramId(telegramUserId);
        List<Transaction> transactionsInCategory = statisticsService
            .getMonthlyCategoryStatistics(userId, category);

        Double totalInCategory = transactionsInCategory.stream()
            .reduce(0.0, (res, tran) -> res + tran.amount(), Double::sum);

        String message = getMessage(category, totalInCategory);
        InlineKeyboardMarkup keyboard = getKeyboardMarkup(transactionsInCategory);
        editMessage(getChatId(update), messageId, message, keyboard);
    }

    private static String getMessage(String category, Double totalInCategory) {
        Month month = LocalDate.now().getMonth();
        return escapeMarkdownV2("""
            Top transactions in %s
            *Category: %s*
            Spent in the category: `%s`""".formatted(formatMonth(month), category, formatNumber(totalInCategory))
        );
    }

    private InlineKeyboardMarkup getKeyboardMarkup(List<Transaction> transactionsInCategory) {
        List<List<InlineKeyboardButton>> rows = transactionsInCategory.stream()
            .map(transaction -> InlineKeyboardButton.builder()
                .text(getTransactionRepresentation(transaction))
                .callbackData(toJson(Map.of("type", "transactions/actions", "transactionId", transaction.id())))
                .build())
            .map(List::of)
            .limit(maxNumberOfStatisticsRecords)
            .toList();
        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build();
    }

    private void editMessage(
        Long chatId,
        Integer messageId,
        String text,
        @Nullable InlineKeyboardMarkup replyKeyboard
    ) {
        EditMessageText editMessage = EditMessageText.builder()
            .chatId(chatId)
            .messageId(messageId)
            .text(text)
            .parseMode(ParseMode.MARKDOWNV2)
            .replyMarkup(replyKeyboard)
            .build();
        telegramApi.execute(editMessage);
    }
}
