package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.csv.CsvWriter;
import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.domain.util.TransactionUtil;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContext;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.callback.CallbackDataExtractor;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.GetMonthlyReportCommand;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import com.github.msemitkin.financie.telegram.util.UpdateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.ByteArrayInputStream;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.TimeZone;

@Component
public class MonthlyReportUpdateHandler extends BaseUpdateHandler {
    private static final Logger logger = LoggerFactory.getLogger(MonthlyReportUpdateHandler.class);

    private final UserService userService;
    private final TransactionService transactionService;
    private final CallbackDataExtractor callbackDataExtractor;
    private final CsvWriter csvWriter;
    private final TelegramApi telegramApi;

    protected MonthlyReportUpdateHandler(
        CallbackService callbackService,
        UserService userService,
        TransactionService transactionService,
        CallbackDataExtractor callbackDataExtractor,
        CsvWriter csvWriter, TelegramApi telegramApi
    ) {
        super(UpdateMatcher.callbackQueryMatcher(callbackService, CallbackType.GET_MONTHLY_REPORT));
        this.userService = userService;
        this.transactionService = transactionService;
        this.callbackDataExtractor = callbackDataExtractor;
        this.csvWriter = csvWriter;
        this.telegramApi = telegramApi;
    }

    @Override
    protected void handleUpdate(Update update) {
        long senderTelegramId = UpdateUtil.getSenderTelegramId(update);
        User user = userService.getUserByTelegramId(senderTelegramId);
        Long chatId = UpdateUtil.getChatId(update);
        long userId = user.id();
        GetMonthlyReportCommand callbackData = callbackDataExtractor
            .getCallbackData(update, GetMonthlyReportCommand.class);
        OffsetDateTime offsetDateTime = callbackData.utcYearMonth();
        OffsetDateTime startOfTheMonth = offsetDateTime.with(TemporalAdjusters.firstDayOfMonth())
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);

        UserContext userContext = UserContextHolder.getContext();
        TimeZone timeZone = userContext.timeZone();
        List<Transaction> transactions = transactionService
            .getTransactions(userId, startOfTheMonth, startOfTheMonth.plusMonths(1))
            .stream()
            .map(tran -> TransactionUtil.atZoneSameInstant(tran, timeZone.toZoneId()))
            .toList();
        byte[] bytes = csvWriter.writeCsvFile(transactions);
        ZonedDateTime zonedDateTime = offsetDateTime.atZoneSameInstant(timeZone.toZoneId());
        Month month = zonedDateTime.getMonth();

        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            telegramApi.execute(SendDocument.builder()
                .chatId(chatId)
                .document(new InputFile(is, "%d-%d Financie export.csv".formatted(
                    zonedDateTime.getYear(), month.getValue())))
                .build());
        } catch (Exception e) {
            logger.error("Failed to export transactions", e);
            telegramApi.execute(SendMessage.builder()
                .chatId(chatId)
                .text("Failed to export transactions, please try again")
                .build());
        }

    }
}
