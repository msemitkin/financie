package com.github.msemitkin.financie.telegram.updatehandler.categories.daily;

import com.github.msemitkin.financie.domain.CategoryStatistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.StatisticsUtil;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.telegram.callback.Callback;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.GetDailyCategoriesCommand;
import com.github.msemitkin.financie.telegram.callback.command.GetDailyCategoryTransactionsCommand;
import com.github.msemitkin.financie.telegram.updatehandler.categories.Response;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.msemitkin.financie.telegram.callback.CallbackType.GET_CATEGORY_TRANSACTIONS_FOR_DAY;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatDate;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.telegram.util.MarkdownUtil.escapeMarkdownV2;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;

@Component
class DailyCategoriesResponseService {
    private final StatisticsService statisticsService;
    private final UserService userService;
    private final CallbackService callbackService;

    DailyCategoriesResponseService(
        StatisticsService statisticsService,
        UserService userService,
        CallbackService callbackService
    ) {
        this.statisticsService = statisticsService;
        this.userService = userService;
        this.callbackService = callbackService;
    }

    Response prepareResponse(Update update, GetDailyCategoriesCommand command) {
        int dayOffset = command.offset();
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
        var command = new GetDailyCategoriesCommand(dayOffset);
        return callbackService.saveCallback(new Callback<>(CallbackType.GET_CATEGORIES_FOR_DAY, command)).toString();
    }

    private List<List<InlineKeyboardButton>> getKeyboard(List<CategoryStatistics> statistics, int dayOffset) {
        List<List<InlineKeyboardButton>> keyboard = statistics.stream()
            .map(tran -> {
                var callback = new Callback<>(
                    GET_CATEGORY_TRANSACTIONS_FOR_DAY,
                    new GetDailyCategoryTransactionsCommand(tran.categoryId(), dayOffset)
                );
                UUID callbackId = callbackService.saveCallback(callback);
                return InlineKeyboardButton.builder()
                    .text("%s : %s".formatted(tran.categoryName(), formatNumber(tran.amount())))
                    .callbackData(callbackId.toString())
                    .build();
            })
            .map(List::of)
            .collect(Collectors.toCollection(ArrayList::new));
        keyboard.add(getPageButtons(dayOffset));
        return keyboard;
    }
}
