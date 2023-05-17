package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.AveragePerDayService;
import com.github.msemitkin.financie.domain.Category;
import com.github.msemitkin.financie.domain.CategoryService;
import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.domain.TransactionUtil;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContext;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.callback.Callback;
import com.github.msemitkin.financie.telegram.callback.CallbackDataExtractor;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.GetMonthlyCategoryTransactionsCommand;
import com.github.msemitkin.financie.telegram.callback.command.GetTransactionActionsCommand;
import com.github.msemitkin.financie.telegram.updatehandler.BaseUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import jakarta.annotation.Nullable;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatMonth;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.telegram.util.MarkdownUtil.escapeMarkdownV2;
import static com.github.msemitkin.financie.telegram.util.TransactionUtil.getTransactionRepresentation;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;
import static com.github.msemitkin.financie.timezone.TimeZoneUtils.getUTCStartOfTheMonthInTimeZone;

@Component
public class GetMonthlyCategoryTransactionsHandler extends BaseUpdateHandler {
    private final UserService userService;
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final AveragePerDayService averagePerDayService;
    private final TelegramApi telegramApi;
    private final CallbackDataExtractor callbackDataExtractor;
    private final CallbackService callbackService;
    private final int maxNumberOfStatisticsRecords;

    public GetMonthlyCategoryTransactionsHandler(
        UserService userService,
        TransactionService transactionService,
        CategoryService categoryService,
        CallbackService callbackService,
        AveragePerDayService averagePerDayService,
        TelegramApi telegramApi,
        CallbackDataExtractor callbackDataExtractor,
        @Value("${com.github.msemitkin.financie.statistics.max-number-of-displayed-records}")
        int maxNumberOfStatisticsRecords
    ) {
        super(UpdateMatcher.callbackQueryMatcher(callbackService, CallbackType.GET_CATEGORY_TRANSACTIONS_FOR_MONTH));
        this.userService = userService;
        this.transactionService = transactionService;
        this.categoryService = categoryService;
        this.averagePerDayService = averagePerDayService;
        this.telegramApi = telegramApi;
        this.callbackDataExtractor = callbackDataExtractor;
        this.callbackService = callbackService;
        this.maxNumberOfStatisticsRecords = maxNumberOfStatisticsRecords;
    }

    @Override
    public void handleUpdate(Update update) {
        UserContext userContext = UserContextHolder.getContext();
        Locale locale = userContext.locale();
        TimeZone timeZone = userContext.timeZone();

        var callbackData = callbackDataExtractor.getCallbackData(update, GetMonthlyCategoryTransactionsCommand.class);
        long categoryId = callbackData.categoryId();
        int offset = callbackData.offset();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        long userTelegramId = getSenderTelegramId(update);
        long chatId = getChatId(update);
        long userId = userService.getUserByTelegramId(userTelegramId).id();
        LocalDateTime startOfMonth = getUTCStartOfTheMonthInTimeZone(timeZone.toZoneId()).plusMonths(offset);
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        Category category = categoryService.getCategory(categoryId);

        List<Transaction> transactionsInCategory = transactionService
            .getTransactions(userId, category.name(), startOfMonth, startOfNextMonth)
            .stream().sorted(Comparator.comparing(Transaction::time).reversed()).toList();

        double totalInCategory = TransactionUtil.sum(transactionsInCategory);
        double averagePerDayInCategory = averagePerDayService
            .getAveragePerDay(totalInCategory, YearMonth.now().plusMonths(offset), timeZone.toZoneId());

        String message = getMessage(category.name(), totalInCategory, averagePerDayInCategory, startOfMonth.getMonth(), locale);
        InlineKeyboardMarkup keyboard = getKeyboardMarkup(transactionsInCategory);
        editMessage(chatId, messageId, message, keyboard);
    }

    private static String getMessage(
        String category,
        double totalInCategory,
        double averagePerDayInCategory,
        Month month,
        Locale locale
    ) {
        var messageTemplate = ResourceService.getValue("transactions-for-month-in-category", locale);
        var params = Map.of(
            "month", formatMonth(month, locale),
            "category", category, "total", formatNumber(totalInCategory),
            "average_per_day", formatNumber(averagePerDayInCategory)
        );
        String message = StringSubstitutor.replace(messageTemplate, params);
        return escapeMarkdownV2(message);
    }

    private InlineKeyboardMarkup getKeyboardMarkup(List<Transaction> transactionsInCategory) {
        List<List<InlineKeyboardButton>> rows = transactionsInCategory.stream()
            .map(transaction -> {
                var callback = new Callback<>(
                    CallbackType.GET_TRANSACTION_ACTIONS,
                    new GetTransactionActionsCommand(transaction.id())
                );
                UUID callbackId = callbackService.saveCallback(callback);
                return InlineKeyboardButton.builder()
                    .text(getTransactionRepresentation(transaction))
                    .callbackData(callbackId.toString())
                    .build();
            })
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
