package com.github.msemitkin.financie.telegram.updatehandler.categories.daily;

import com.github.msemitkin.financie.domain.CategoryStatistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.StatisticsUtil;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.telegram.updatehandler.categories.Response;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatDate;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.telegram.util.JsonUtil.toJson;
import static com.github.msemitkin.financie.telegram.util.MarkdownUtil.escapeMarkdownV2;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;

@Component
class DailyCategoriesResponseService {
    private final StatisticsService statisticsService;
    private final UserService userService;

    DailyCategoriesResponseService(
        StatisticsService statisticsService,
        UserService userService
    ) {
        this.statisticsService = statisticsService;
        this.userService = userService;
    }

    Response prepareResponse(Update update) {
        int dayOffset = getOffset(update);
        long userId = userService.getUserByTelegramId(getSenderTelegramId(update)).id();
        List<CategoryStatistics> statistics = getDailyCategories(userId, dayOffset);
        if (statistics.isEmpty()) {
            String message = "No transactions " +
                             (dayOffset == 0 ? "today" : "on " + formatDate(LocalDate.now().plusDays(dayOffset)));
            var keyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(getPageButtons(dayOffset)).build();
            return new Response(escapeMarkdownV2(message), keyboardMarkup);
        } else {
            double total = StatisticsUtil.sum(statistics);
            String message = escapeMarkdownV2("""
                %s
                Total: `%s`
                """.formatted(formatDate(LocalDate.now().plusDays(dayOffset)), formatNumber(total)));
            var keyboardMarkup = InlineKeyboardMarkup.builder().keyboard(getKeyboard(statistics, dayOffset)).build();
            return new Response(message, keyboardMarkup);
        }
    }

    private int getOffset(Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .map(data -> new Gson().fromJson(data, JsonObject.class))
            .map(json -> json.get("offset"))
            .map(JsonElement::getAsInt)
            .orElse(0);
    }

    private List<CategoryStatistics> getDailyCategories(long userId, int dayOffset) {
        return statisticsService.getStatistics(
            userId,
            LocalDate.now().plusDays(dayOffset).atStartOfDay(),
            LocalDate.now().plusDays(dayOffset).plusDays(1).atStartOfDay()
        );
    }

    private List<InlineKeyboardButton> getPageButtons(int dayOffset) {
        var leftButton = button("⬅️", getPageButtonCallbackData(dayOffset - 1));
        var rightButton = button("➡️", getPageButtonCallbackData(dayOffset + 1));
        return dayOffset == 0 ? List.of(leftButton) : List.of(leftButton, rightButton);
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
            .text(text)
            .callbackData(callbackData)
            .build();
    }

    private String getPageButtonCallbackData(int dayOffset) {
        return toJson(Map.of("tp", "day_cat", "offset", dayOffset));
    }

    private List<List<InlineKeyboardButton>> getKeyboard(List<CategoryStatistics> statistics, int dayOffset) {
        List<List<InlineKeyboardButton>> keyboard = statistics.stream()
            .map(tran -> InlineKeyboardButton.builder()
                .text("%s : %s".formatted(tran.categoryName(), formatNumber(tran.amount())))
                .callbackData(toJson(Map.of(
                    "tp", "day_trans",
                    "cat_id", tran.categoryId(),
                    "offset", dayOffset)))
                .build())
            .map(List::of)
            .collect(Collectors.toCollection(ArrayList::new));
        keyboard.add(getPageButtons(dayOffset));
        return keyboard;
    }
}
