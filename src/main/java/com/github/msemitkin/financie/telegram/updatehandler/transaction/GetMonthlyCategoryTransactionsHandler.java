package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractQueryHandler;
import com.google.gson.JsonObject;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatMonth;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.telegram.util.JsonUtil.toJson;
import static com.github.msemitkin.financie.telegram.util.MarkdownUtil.escapeMarkdownV2;
import static com.github.msemitkin.financie.telegram.util.TransactionUtil.getTransactionRepresentation;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;

@Component
public class GetMonthlyCategoryTransactionsHandler extends AbstractQueryHandler {
    private final UserService userService;
    private final TransactionService transactionService;
    private final TelegramApi telegramApi;
    private final int maxNumberOfStatisticsRecords;

    public GetMonthlyCategoryTransactionsHandler(
        UserService userService,
        TransactionService transactionService,
        TelegramApi telegramApi,
        @Value("${com.github.msemitkin.financie.statistics.max-number-of-displayed-records}")
        int maxNumberOfStatisticsRecords
    ) {
        super("monthly_stats");
        this.userService = userService;
        this.transactionService = transactionService;
        this.telegramApi = telegramApi;
        this.maxNumberOfStatisticsRecords = maxNumberOfStatisticsRecords;
    }

    @Override
    public void handleUpdate(Update update) {
        JsonObject jsonObject = getCallbackData(update);
        String category = jsonObject.get("category").getAsString();
        int offset = jsonObject.get("offset").getAsInt();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        long telegramUserId = getSenderTelegramId(update);
        long userId = userService.getOrCreateUserByTelegramId(telegramUserId);
        LocalDateTime startOfMonth = YearMonth.now().plusMonths(offset).atDay(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        List<Transaction> transactionsInCategory = transactionService
            .getTransactions(userId, category, startOfMonth, startOfNextMonth);

        Double totalInCategory = transactionsInCategory.stream()
            .reduce(0.0, (res, tran) -> res + tran.amount(), Double::sum);

        String message = getMessage(category, totalInCategory);
        InlineKeyboardMarkup keyboard = getKeyboardMarkup(transactionsInCategory);
        editMessage(getChatId(update), messageId, message, keyboard);
    }

    private static String getMessage(String category, Double totalInCategory) {
        Month month = LocalDate.now().getMonth();
        return escapeMarkdownV2("""
            Top transactions in %s
            *Category: %s*
            Spent in the category: `%s`""".formatted(formatMonth(month), category, formatNumber(totalInCategory))
        );
    }

    private InlineKeyboardMarkup getKeyboardMarkup(List<Transaction> transactionsInCategory) {
        List<List<InlineKeyboardButton>> rows = transactionsInCategory.stream()
            .map(transaction -> InlineKeyboardButton.builder()
                .text(getTransactionRepresentation(transaction))
                .callbackData(toJson(Map.of("type", "transactions/actions", "transactionId", transaction.id())))
                .build())
            .map(List::of)
            .limit(maxNumberOfStatisticsRecords)
            .toList();
        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build();
    }

    private void editMessage(
        Long chatId,
        Integer messageId,
        String text,
        @Nullable InlineKeyboardMarkup replyKeyboard
    ) {
        EditMessageText editMessage = EditMessageText.builder()
            .chatId(chatId)
            .messageId(messageId)
            .text(text)
            .parseMode(ParseMode.MARKDOWNV2)
            .replyMarkup(replyKeyboard)
            .build();
        telegramApi.execute(editMessage);
    }
}
