package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.CategoryStatistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
    private final int maxNumberOfStatisticsRecords;

    public GetMonthlyStatisticsHandler(
        TelegramApi telegramApi,
        TransactionService transactionService,
        StatisticsService statisticsService,
        @Value("${com.github.msemitkin.financie.statistics.max-number-of-displayed-records}")
        int maxNumberOfStatisticsRecords
    ) {
        super(BotCommand.MONTHLY_STATISTICS.getCommand());
        this.telegramApi = telegramApi;
        this.transactionService = transactionService;
        this.statisticsService = statisticsService;
        this.maxNumberOfStatisticsRecords = maxNumberOfStatisticsRecords;
    }

    @Override
    public void handleUpdate(Update update) {
        Long userTelegramId = requireNonNull(getSenderTelegramId(update));
        Long chatId = getChatId(update);
        long userId = transactionService.getOrCreateUserByTelegramId(userTelegramId);
        List<CategoryStatistics> statistics = statisticsService
            .getStatistics(userId, YearMonth.now().atDay(1).atStartOfDay(), LocalDateTime.now());
        String month = formatMonth(LocalDate.now().getMonth());

        if (statistics.isEmpty()) {
            String text = "No transactions in " + month;
            sendMessage(chatId, text, null);
            return;
        }

        Double total = statistics.stream().reduce(0.0, (result, stat) -> result + stat.amount(), Double::sum);

        InlineKeyboardMarkup keyboard = getKeyboard(statistics);
        String text = getText(total, month);
        sendMessage(chatId, text, keyboard);
    }

    private String getText(double total, String month) {
        return "Total spent in " + month + ": `" + total + "`\n" + "Top categories:";
    }

    private InlineKeyboardMarkup getKeyboard(List<CategoryStatistics> statistics) {
        List<List<InlineKeyboardButton>> rows = statistics.stream()
            .map(stats -> {
                String text = "%.1f: %s".formatted(stats.amount(), stats.category());
                String callbackData = toJson(Map.of(
                    "type", "monthly_stats",
                    "category", stats.category()
                ));
                return inlineButton(text, callbackData);
            })
            .map(List::of)
            .limit(maxNumberOfStatisticsRecords)
            .toList();
        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build();
    }

    private void sendMessage(
        Long chatId,
        String text,
        @Nullable ReplyKeyboard replyKeyboard
    ) {
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .parseMode(ParseMode.MARKDOWNV2)
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
