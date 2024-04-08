package com.github.msemitkin.financie.telegram.updatehandler.categories.daily;

import com.github.msemitkin.financie.domain.CategoryStatistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.StatisticsUtil;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.auth.UserContext;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.callback.Callback;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.AddTransactionCommand;
import com.github.msemitkin.financie.telegram.callback.command.GetDailyCategoriesCommand;
import com.github.msemitkin.financie.telegram.callback.command.GetDailyCategoryTransactionsCommand;
import com.github.msemitkin.financie.telegram.updatehandler.categories.Response;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.msemitkin.financie.telegram.callback.CallbackType.ADD_TRANSACTION;
import static com.github.msemitkin.financie.telegram.callback.CallbackType.GET_CATEGORY_TRANSACTIONS_FOR_DAY;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatDate;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.telegram.util.MarkdownUtil.escapeMarkdownV2;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;
import static com.github.msemitkin.financie.timezone.TimeZoneUtils.getUTCStartOfTheDayInTimeZone;

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
        UserContext userContext = UserContextHolder.getContext();
        Locale userLocale = userContext.locale();
        ZoneId timeZoneId = userContext.timeZone().toZoneId();

        int dayOffset = command.offset();
        long userId = userService.getUserByTelegramId(getSenderTelegramId(update)).id();
        List<CategoryStatistics> statistics = getDailyCategories(userId, dayOffset, timeZoneId);

        if (statistics.isEmpty()) {

            String message = StringSubstitutor.replace(
                ResourceService.getValue("no-transactions-on-date", userLocale),
                Map.of("date", formatDate(LocalDate.now(timeZoneId).plusDays(dayOffset)))
            );
            var keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(getPageButtons(dayOffset, userLocale))
                .keyboardRow(new InlineKeyboardRow(getAddTransactionButton(dayOffset, userLocale)))
                .build();
            return new Response(escapeMarkdownV2(message), keyboardMarkup);
        } else {
            double total = StatisticsUtil.sum(statistics);
            String message = StringSubstitutor.replace(
                ResourceService.getValue("total-on-date", userLocale),
                Map.of("date", formatDate(LocalDate.now(timeZoneId).plusDays(dayOffset)), "amount", formatNumber(total))
            );
            message = escapeMarkdownV2(message);
            var keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(getKeyboard(statistics, dayOffset, userLocale))
                .build();
            return new Response(message, keyboardMarkup);
        }
    }

    private List<CategoryStatistics> getDailyCategories(long userId, int dayOffset, ZoneId zoneId) {
        LocalDateTime startOfTheDay = getUTCStartOfTheDayInTimeZone(zoneId).plusDays(dayOffset);

        return statisticsService.getStatistics(userId, startOfTheDay, startOfTheDay.plusDays(1));
    }

    private InlineKeyboardRow getPageButtons(int dayOffset, Locale locale) {
        var leftButton = button(ResourceService.getValue("button.left", locale),
            getPageButtonCallbackData(dayOffset - 1));
        var rightButton = button(ResourceService.getValue("button.right", locale),
            getPageButtonCallbackData(dayOffset + 1));
        return new InlineKeyboardRow(dayOffset == 0 ? List.of(leftButton) : List.of(leftButton, rightButton));
    }

    private String getPageButtonCallbackData(int dayOffset) {
        var command = new GetDailyCategoriesCommand(dayOffset);
        return callbackService.saveCallback(new Callback<>(CallbackType.GET_CATEGORIES_FOR_DAY, command)).toString();
    }

    private List<InlineKeyboardRow> getKeyboard(
        List<CategoryStatistics> statistics,
        int dayOffset,
        Locale locale
    ) {
        List<InlineKeyboardRow> keyboard = statistics.stream()
            .map(tran -> getInlineButton(tran, dayOffset, locale))
            .map(InlineKeyboardRow::new)
            .collect(Collectors.toCollection(ArrayList::new));
        keyboard.add(getPageButtons(dayOffset, locale));
        keyboard.add(new InlineKeyboardRow(getAddTransactionButton(dayOffset, locale)));
        return keyboard;
    }

    private InlineKeyboardButton getInlineButton(CategoryStatistics category, int dayOffset, Locale locale) {
        var callback = new Callback<>(GET_CATEGORY_TRANSACTIONS_FOR_DAY,
            new GetDailyCategoryTransactionsCommand(category.categoryId(), dayOffset));
        UUID callbackId = callbackService.saveCallback(callback);
        var text = StringSubstitutor.replace(
            ResourceService.getValue("category-transaction-format", locale),
            Map.of("category", category.categoryName(), "amount", formatNumber(category.amount()))
        );
        return button(text, callbackId.toString());
    }

    private InlineKeyboardButton getAddTransactionButton(int dayOffset, Locale locale) {
        var callback = new Callback<>(ADD_TRANSACTION, new AddTransactionCommand(LocalDate.now().plusDays(dayOffset)));
        UUID callbackId = callbackService.saveCallback(callback);
        return button(ResourceService.getValue("button.add-transaction", locale), callbackId.toString());
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
            .text(text)
            .callbackData(callbackData)
            .build();
    }
}
