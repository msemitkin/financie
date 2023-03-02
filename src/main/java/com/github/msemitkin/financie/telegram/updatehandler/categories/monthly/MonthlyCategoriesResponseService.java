package com.github.msemitkin.financie.telegram.updatehandler.categories.monthly;

import com.github.msemitkin.financie.domain.CategoryStatistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.telegram.updatehandler.categories.Response;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatMonth;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.telegram.util.JsonUtil.toJson;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;

@Service
public class MonthlyCategoriesResponseService {
    private final UserService userService;
    private final StatisticsService statisticsService;
    private final int maxNumberOfStatisticsRecords;

    public MonthlyCategoriesResponseService(
        UserService userService,
        StatisticsService statisticsService,
        @Value("${com.github.msemitkin.financie.statistics.max-number-of-displayed-records}")
        int maxNumberOfStatisticsRecords
    ) {
        this.userService = userService;
        this.statisticsService = statisticsService;
        this.maxNumberOfStatisticsRecords = maxNumberOfStatisticsRecords;
    }

    Response getResponse(Update update) {
        int monthOffset = getOffset(update);
        long userTelegramId = getSenderTelegramId(update);
        long userId = userService.getOrCreateUserByTelegramId(userTelegramId);

        LocalDateTime startOfMonth = YearMonth.now().plusMonths(monthOffset).atDay(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);
        List<CategoryStatistics> statistics = statisticsService.getStatistics(userId, startOfMonth, startOfNextMonth);
        String month = formatMonth(startOfMonth.getMonth());

        if (statistics.isEmpty()) {
            String text = "No transactions in " + month;
            return new Response(text, InlineKeyboardMarkup.builder().keyboardRow(getPageButtons(monthOffset)).build());
        }

        Double total = statistics.stream().reduce(0.0, (result, stat) -> result + stat.amount(), Double::sum);

        InlineKeyboardMarkup keyboard = getKeyboard(statistics, monthOffset);
        String text = getText(total, month);
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
        return toJson(Map.of("tp", "monthly_categories", "offset", offset));
    }

    private String getText(double total, String month) {
        return """
            Total spent in %s: `%s`
            Top categories:""".formatted(month, formatNumber(total));
    }

    private InlineKeyboardMarkup getKeyboard(List<CategoryStatistics> statistics, int monthOffset) {
        List<List<InlineKeyboardButton>> rows = statistics.stream()
            .map(stats -> {
                String text = "%s: %s".formatted(formatNumber(stats.amount()), stats.categoryName());
                String callbackData = toJson(Map.of(
                    "tp", "monthly_stats",
                    "cat_id", stats.categoryId(),
                    "offset", monthOffset
                ));
                return button(text, callbackData);
            })
            .map(List::of)
            .limit(maxNumberOfStatisticsRecords)
            .collect(Collectors.toCollection(ArrayList::new));
        rows.add(getPageButtons(monthOffset));
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private int getOffset(Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .map(data -> new Gson().fromJson(data, JsonObject.class))
            .map(json -> json.get("offset"))
            .map(JsonElement::getAsInt)
            .orElse(0);
    }
}
