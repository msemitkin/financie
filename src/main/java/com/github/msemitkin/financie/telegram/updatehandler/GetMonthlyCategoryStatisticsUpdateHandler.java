package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatMonth;
import static com.github.msemitkin.financie.telegram.util.JsonUtil.toJson;
import static com.github.msemitkin.financie.telegram.util.TransactionUtil.getTransactionRepresentation;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;
import static java.util.Objects.requireNonNull;

@Component
public class GetMonthlyCategoryStatisticsUpdateHandler implements UpdateHandler {
    private final AbsSender absSender;
    private final TransactionService transactionService;
    private final StatisticsService statisticsService;

    public GetMonthlyCategoryStatisticsUpdateHandler(
        AbsSender absSender, TransactionService transactionService,
        StatisticsService statisticsService
    ) {
        this.absSender = absSender;
        this.transactionService = transactionService;
        this.statisticsService = statisticsService;
    }

    @Override
    public boolean canHandle(Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .map(callbackData -> new Gson().fromJson(callbackData, JsonObject.class))
            .map(json -> json.get("type").getAsString())
            .map("monthly_stats"::equals)
            .orElse(false);
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

        InlineKeyboardMarkup keyboard = getKeyboardMarkup(transactionsInCategory);
        Month month = LocalDate.now().getMonth();
        String message = """
            Transactions in %s
            Category: %s""".formatted(formatMonth(month), category);
        editMessage(getChatId(update), messageId, message, keyboard);
    }

    private InlineKeyboardMarkup getKeyboardMarkup(List<Transaction> transactionsInCategory) {
        List<List<InlineKeyboardButton>> rows = transactionsInCategory.stream()
            .map(transaction -> InlineKeyboardButton.builder()
                .text(getTransactionRepresentation(transaction))
                .callbackData(toJson(Map.of("type", "transactions/actions", "transactionId", transaction.id())))
                .build())
            .map(List::of)
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
            .replyMarkup(replyKeyboard)
            .build();
        try {
            absSender.execute(editMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
