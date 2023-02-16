package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.CategoryStatistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatMonth;
import static com.github.msemitkin.financie.telegram.util.JsonUtil.toJson;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;
import static java.util.Objects.requireNonNull;

@Component
public class GetMonthlyStatisticsHandler extends AbstractTextCommandHandler {
    private final TelegramApi telegramApi;
    private final TransactionService transactionService;
    private final StatisticsService statisticsService;

    public GetMonthlyStatisticsHandler(
        TelegramApi telegramApi,
        TransactionService transactionService,
        StatisticsService statisticsService
    ) {
        super(BotCommand.MONTHLY_STATISTICS.getCommand());
        this.telegramApi = telegramApi;
        this.transactionService = transactionService;
        this.statisticsService = statisticsService;
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
            sendMessage(chatId, text, null);
            return;
        }

        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder = InlineKeyboardMarkup.builder();
        statistics.forEach(stats -> {
            String text = "%.1f: %s".formatted(stats.amount(), stats.category());
            String callbackData = toJson(Map.of(
                "type", "monthly_stats",
                "category", stats.category()
            ));
            keyboardBuilder.keyboardRow(List.of(inlineButton(text, callbackData)));
        });
        InlineKeyboardMarkup keyboard = keyboardBuilder.build();
        sendMessage(chatId, "Transactions in ".concat(month), keyboard);
    }

    private void sendMessage(
        Long chatId,
        String text,
        @Nullable ReplyKeyboard replyKeyboard
    ) {
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .replyMarkup(replyKeyboard)
            .build();
        telegramApi.execute(sendMessage);
    }

    private InlineKeyboardButton inlineButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
            .text(text)
            .callbackData(callbackData)
            .build();
    }
}
