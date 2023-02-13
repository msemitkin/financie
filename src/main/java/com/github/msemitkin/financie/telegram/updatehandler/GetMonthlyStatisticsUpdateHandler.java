package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.CategoryStatistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import com.google.gson.JsonObject;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatMonth;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;
import static java.util.Objects.requireNonNull;

@Component
public class GetMonthlyStatisticsUpdateHandler implements UpdateHandler {
    private final AbsSender absSender;
    private final TransactionService transactionService;
    private final StatisticsService statisticsService;

    public GetMonthlyStatisticsUpdateHandler(
        AbsSender absSender,
        TransactionService transactionService,
        StatisticsService statisticsService
    ) {
        this.absSender = absSender;
        this.transactionService = transactionService;
        this.statisticsService = statisticsService;
    }

    @Override
    public boolean canHandle(Update update) {
        return Optional.ofNullable(update.getMessage())
            .map(Message::getText)
            .map(BotCommand.MONTHLY_STATISTICS.getCommand()::equals)
            .orElse(false);
    }

    @Override
    public void handleUpdate(Update update) {
        Long userTelegramId = requireNonNull(getSenderTelegramId(update));
        Long chatId = getChatId(update);
        long userId = transactionService.getOrCreateUserByTelegramId(userTelegramId);
        List<CategoryStatistics> statistics = statisticsService
            .getMonthlyStatistics(userId);
        String month = formatMonth(LocalDate.now().getMonth());

        if (statistics.isEmpty()) {
            String text = "No transactions in " + month;
            sendMessage(chatId, text, null, null);
            return;
        }

        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder = InlineKeyboardMarkup.builder();
        statistics.forEach(stats -> {
            String text = "%.1f: %s".formatted(stats.amount(), stats.category());
            String callbackData = toJsonFormat(Map.of(
                "type", "monthly_stats",
                "category", stats.category()
            ));
            keyboardBuilder.keyboardRow(List.of(inlineButton(text, callbackData)));
        });
        InlineKeyboardMarkup keyboard = keyboardBuilder.build();
        sendMessage(chatId, month.concat(" statistics"), null, keyboard);
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
}
