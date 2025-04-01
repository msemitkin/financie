package com.github.msemitkin.financie.telegram.updatehandler.common;

import com.github.msemitkin.financie.domain.Statistics;
import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.timezone.TimeZoneUtils.getUTCStartOfTheDayInTimeZone;
import static com.github.msemitkin.financie.timezone.TimeZoneUtils.getUTCStartOfTheMonthInTimeZone;

@Service
public class SendSuccessfullySavedTransactionService {
    private final StatisticsService statisticsService;
    private final TelegramApi telegramApi;

    public SendSuccessfullySavedTransactionService(StatisticsService statisticsService, TelegramApi telegramApi) {
        this.statisticsService = statisticsService;
        this.telegramApi = telegramApi;
    }

    public void sendSuccessfullySavedTransaction(
        Long chatId,
        Long userId,
        Integer messageId,
        String category,
        Locale locale,
        ZoneId zoneId
    ) {
        sendSuccessfullySavedTransaction(chatId, userId, messageId, category, locale, zoneId, null);
    }

    public void sendSuccessfullySavedTransaction(
        Long chatId,
        Long userId,
        Integer messageId,
        String category,
        Locale locale,
        ZoneId zoneId,
        ReplyKeyboardMarkup replyKeyboardMarkup
    ) {
        LocalDateTime startOfTheDay = getUTCStartOfTheDayInTimeZone(zoneId);
        LocalDateTime endOfTheDay = startOfTheDay.plusDays(1);
        Statistics dailyStatistics = statisticsService.getStatistics(userId, category, startOfTheDay, endOfTheDay);

        LocalDateTime startOfTheMonth = getUTCStartOfTheMonthInTimeZone(zoneId);
        Statistics monthlyStatistics = statisticsService.getStatistics(userId, category, startOfTheMonth, endOfTheDay);

        String reply = StringSubstitutor.replace(
            ResourceService.getValue("transaction-saved-reply", locale),
            Map.of(
                "today", formatNumber(dailyStatistics.total()),
                "this_month", formatNumber(monthlyStatistics.total()),
                "this_month_in_category", formatNumber(monthlyStatistics.totalInCategory())
            )
        );
        telegramApi.execute(SendMessage.builder()
            .chatId(chatId)
            .text(reply)
            .parseMode(ParseMode.MARKDOWNV2)
            .replyMarkup(replyKeyboardMarkup)
            .replyToMessageId(messageId)
            .build());
    }
}
