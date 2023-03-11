package com.github.msemitkin.financie.telegram.updatehandler.categories.monthly;

import com.github.msemitkin.financie.domain.AveragePerDayService;
import com.github.msemitkin.financie.domain.CategoryStatistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.telegram.callback.Callback;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.GetMonthlyCategoriesCommand;
import com.github.msemitkin.financie.telegram.callback.command.GetMonthlyCategoryTransactionsCommand;
import com.github.msemitkin.financie.telegram.updatehandler.categories.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.msemitkin.financie.domain.StatisticsUtil.sum;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatMonth;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;

@Service
public class MonthlyCategoriesResponseService {
    private final UserService userService;
    private final StatisticsService statisticsService;
    private final CallbackService callbackService;
    private final AveragePerDayService averagePerDayService;
    private final int maxNumberOfStatisticsRecords;

    public MonthlyCategoriesResponseService(
        UserService userService,
        StatisticsService statisticsService,
        CallbackService callbackService,
        AveragePerDayService averagePerDayService,
        @Value("${com.github.msemitkin.financie.statistics.max-number-of-displayed-records}")
        int maxNumberOfStatisticsRecords
    ) {
        this.userService = userService;
        this.statisticsService = statisticsService;
        this.callbackService = callbackService;
        this.averagePerDayService = averagePerDayService;
        this.maxNumberOfStatisticsRecords = maxNumberOfStatisticsRecords;
    }

    Response getResponse(Update update, GetMonthlyCategoriesCommand command) {
        int monthOffset = command.offset();
        long userTelegramId = getSenderTelegramId(update);
        long userId = userService.getUserByTelegramId(userTelegramId).id();

        LocalDateTime startOfMonth = YearMonth.now().plusMonths(monthOffset).atDay(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);
        List<CategoryStatistics> statistics = statisticsService.getStatistics(userId, startOfMonth, startOfNextMonth);
        String month = formatMonth(startOfMonth.getMonth());

        if (statistics.isEmpty()) {
            String text = "No transactions in " + month;
            return new Response(text, InlineKeyboardMarkup.builder().keyboardRow(getPageButtons(monthOffset)).build());
        }

        double total = sum(statistics);
        double averagePerDay = averagePerDayService.getAveragePerDay(total, YearMonth.now().plusMonths(monthOffset));

        InlineKeyboardMarkup keyboard = getKeyboard(statistics, monthOffset);
        String text = getText(total, averagePerDay, month);
        return new Response(text, keyboard);
    }


    private List<InlineKeyboardButton> getPageButtons(int monthOffset) {
        var leftButton = button("⬅️", getPageButtonCallbackData(monthOffset - 1));
        var rightButton = button("➡️", getPageButtonCallbackData(monthOffset + 1));
        return monthOffset == 0 ? List.of(leftButton) : List.of(leftButton, rightButton);
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
            .text(text)
            .callbackData(callbackData)
            .build();
    }

    private String getPageButtonCallbackData(int offset) {
        var callback = new Callback<>(CallbackType.GET_CATEGORIES_FOR_MONTH, new GetMonthlyCategoriesCommand(offset));
        UUID callbackId = callbackService.saveCallback(callback);
        return callbackId.toString();
    }

    private String getText(double total, double average, String month) {
        return """
            Total spent in %s: `%s`
            Average per day: `%s`
            Top categories:""".formatted(month, formatNumber(total), formatNumber(average));
    }

    private InlineKeyboardMarkup getKeyboard(List<CategoryStatistics> statistics, int monthOffset) {
        List<List<InlineKeyboardButton>> rows = statistics.stream()
            .map(stats -> {
                String text = "%s: %s".formatted(formatNumber(stats.amount()), stats.categoryName());
                var callback = new Callback<>(
                    CallbackType.GET_CATEGORY_TRANSACTIONS_FOR_MONTH,
                    new GetMonthlyCategoryTransactionsCommand(stats.categoryId(), monthOffset)
                );
                UUID callbackId = callbackService.saveCallback(callback);
                return button(text, callbackId.toString());
            })
            .map(List::of)
            .limit(maxNumberOfStatisticsRecords)
            .collect(Collectors.toCollection(ArrayList::new));
        rows.add(getPageButtons(monthOffset));
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}
