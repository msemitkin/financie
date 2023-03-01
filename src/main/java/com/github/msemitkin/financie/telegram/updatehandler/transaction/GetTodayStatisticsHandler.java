package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.CategoryStatistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.StatisticsUtil;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import com.github.msemitkin.financie.telegram.updatehandler.UpdateHandler;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
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
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;

@Component
public class GetTodayStatisticsHandler implements UpdateHandler {
    private final UserService userService;
    private final StatisticsService statisticsService;
    private final TelegramApi telegramApi;

    protected GetTodayStatisticsHandler(
        UserService userService,
        StatisticsService statisticsService,
        TelegramApi telegramApi) {
        this.userService = userService;
        this.statisticsService = statisticsService;
        this.telegramApi = telegramApi;
    }

    @Override
    public boolean canHandle(Update update) {
        boolean isTextCommand = Optional.ofNullable(update.getMessage())
            .map(Message::getText)
            .map(BotCommand.TODAY.getCommand()::equals)
            .orElse(false);
        boolean isCallback = Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .map(data -> new Gson().fromJson(data, JsonObject.class))
            .map(json -> json.get("type"))
            .map(JsonElement::getAsString)
            .map("daily_categories"::contains)
            .orElse(false);
        return isTextCommand || isCallback;
    }

    @Override
    public void handleUpdate(Update update) {
        long userId = userService.getOrCreateUserByTelegramId(getSenderTelegramId(update));
        int dayOffset = getOffset(update);
        List<CategoryStatistics> statistics = getDailyCategories(userId, dayOffset);
        long chatId = getChatId(update);
        if (statistics.isEmpty()) {
            returnNoTransactions(dayOffset, chatId, update);
        } else {
            returnTransactions(dayOffset, chatId, statistics, update);
        }
    }

    private List<CategoryStatistics> getDailyCategories(long userId, int dayOffset) {
        return statisticsService.getStatistics(
            userId,
            LocalDate.now().plusDays(dayOffset).atStartOfDay(),
            LocalDate.now().plusDays(dayOffset).plusDays(1).atStartOfDay()
        );
    }

    private List<List<InlineKeyboardButton>> getKeyboard(List<CategoryStatistics> statistics, int dayOffset) {
        List<List<InlineKeyboardButton>> keyboard = statistics.stream()
            .map(tran -> InlineKeyboardButton.builder()
                .text("%s : %s".formatted(tran.category(), formatNumber(tran.amount())))
                .callbackData(toJson(Map.of("type", "day_trans", "category", tran.category())))
                .build())
            .map(List::of)
            .collect(Collectors.toCollection(ArrayList::new));
        keyboard.add(getPageButtons(dayOffset));
        return keyboard;
    }

    private boolean isCallback(Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .isPresent();
    }

    private int getOffset(Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .map(data -> new Gson().fromJson(data, JsonObject.class))
            .map(json -> json.get("offset"))
            .map(JsonElement::getAsInt)
            .orElse(0);
    }

    private void returnNoTransactions(int dayOffset, long chatId, Update update) {
        String message = "No transactions " +
                         (dayOffset == 0 ? "today" : "on " + formatDate(LocalDate.now().plusDays(dayOffset)));
        var keyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(getPageButtons(dayOffset)).build();
        if (isCallback(update)) {
            telegramApi.execute(EditMessageText.builder()
                .chatId(chatId)
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .text(message)
                .replyMarkup(keyboardMarkup)
                .build());
        } else {
            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .replyMarkup(keyboardMarkup)
                .build());
        }
    }

    private void returnTransactions(int dayOffset, long chatId, List<CategoryStatistics> statistics, Update update) {
        double total = StatisticsUtil.sum(statistics);
        String text = escapeMarkdownV2("""
            %s
            Total: `%s`
            """.formatted(formatDate(LocalDate.now().plusDays(dayOffset)), formatNumber(total)));
        var keyboardMarkup = InlineKeyboardMarkup.builder().keyboard(getKeyboard(statistics, dayOffset)).build();
        if (isCallback(update)) {
            telegramApi.execute(EditMessageText.builder()
                .chatId(chatId)
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .text(text)
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(keyboardMarkup)
                .build());
        } else {
            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(keyboardMarkup)
                .build());
        }
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

    private static String getPageButtonCallbackData(int dayOffset) {
        return toJson(Map.of("type", "daily_categories", "offset", dayOffset));
    }
}
