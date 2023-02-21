package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.CategoryStatistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.StatisticsUtil;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import com.github.msemitkin.financie.telegram.util.JsonUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;
import static java.util.Objects.requireNonNull;

@Component
public class GetTodayStatisticsHandler extends AbstractTextCommandHandler {
    private final TransactionService transactionService;
    private final StatisticsService statisticsService;
    private final TelegramApi telegramApi;

    protected GetTodayStatisticsHandler(
        TransactionService transactionService,
        StatisticsService statisticsService,
        TelegramApi telegramApi) {
        super(BotCommand.TODAY.getCommand());
        this.transactionService = transactionService;
        this.statisticsService = statisticsService;
        this.telegramApi = telegramApi;
    }

    @Override
    public void handleUpdate(Update update) {
        long senderTelegramId = requireNonNull(getSenderTelegramId(update));
        long userId = transactionService.getOrCreateUserByTelegramId(senderTelegramId);
        List<CategoryStatistics> statistics = statisticsService
            .getStatistics(userId, LocalDate.now().atStartOfDay(), LocalDateTime.now());

        long chatId = requireNonNull(getChatId(update));
        if (statistics.isEmpty()) {
            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .text("No transactions today")
                .build());
        } else {
            double total = StatisticsUtil.sum(statistics);
            List<List<InlineKeyboardButton>> rows = getKeyboard(statistics);
            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .text("Today: `%.1f`".formatted(total))
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rows).build())
                .build());
        }
    }

    private List<List<InlineKeyboardButton>> getKeyboard(List<CategoryStatistics> statistics) {
        return statistics.stream()
            .map(tran -> InlineKeyboardButton.builder()
                .text("%s : %.1f".formatted(tran.category(), tran.amount()))
                .callbackData(JsonUtil.toJson(Map.of("type", "day_trans", "category", tran.category())))
                .build())
            .map(List::of)
            .toList();
    }
}
