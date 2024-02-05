package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.Category;
import com.github.msemitkin.financie.domain.CategoryService;
import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.domain.TransactionUtil;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.ResponseSender;
import com.github.msemitkin.financie.telegram.auth.UserContext;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.callback.Callback;
import com.github.msemitkin.financie.telegram.callback.CallbackDataExtractor;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.GetDailyCategoryTransactionsCommand;
import com.github.msemitkin.financie.telegram.callback.command.GetTransactionActionsCommand;
import com.github.msemitkin.financie.telegram.updatehandler.BaseUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import com.github.msemitkin.financie.telegram.util.MarkdownUtil;
import com.github.msemitkin.financie.timezone.TimeZoneUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatDate;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getAccessibleMessageId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getFrom;

@Component
public class GetDailyTransactionsHandler extends BaseUpdateHandler {
    private final TransactionService transactionService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ResponseSender responseSender;
    private final CallbackDataExtractor callbackDataExtractor;
    private final CallbackService callbackService;

    public GetDailyTransactionsHandler(
        TransactionService transactionService,
        UserService userService,
        CategoryService categoryService,
        CallbackService callbackService,
        ResponseSender responseSender,
        CallbackDataExtractor callbackDataExtractor
    ) {
        super(UpdateMatcher.callbackQueryMatcher(callbackService, CallbackType.GET_CATEGORY_TRANSACTIONS_FOR_DAY));
        this.transactionService = transactionService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.responseSender = responseSender;
        this.callbackDataExtractor = callbackDataExtractor;
        this.callbackService = callbackService;
    }

    @Override
    public void handleUpdate(Update update) {
        UserContext userContext = UserContextHolder.getContext();
        Locale locale = userContext.locale();
        ZoneId timeZoneId = userContext.timeZone().toZoneId();

        long chatId = getChatId(update);
        long userTelegramId = getFrom(update).getId();
        long userId = userService.getUserByTelegramId(userTelegramId).id();
        var callbackData = callbackDataExtractor.getCallbackData(update, GetDailyCategoryTransactionsCommand.class);
        long categoryId = callbackData.categoryId();
        int offset = callbackData.offset();
        LocalDateTime startOfDay = TimeZoneUtils.getUTCStartOfTheDayInTimeZone(timeZoneId).plusDays(offset);
        LocalDateTime startOfNextDay = startOfDay.plusDays(1);
        Category category = categoryService.getCategory(categoryId);
        List<Transaction> transactions = transactionService
            .getTransactions(userId, category.name(), startOfDay, startOfNextDay)
            .stream()
            .sorted(Comparator.comparing(Transaction::time).reversed())
            .toList();

        Integer messageId = getAccessibleMessageId(update.getCallbackQuery().getMessage());

        LocalDate date = LocalDate.now(timeZoneId).plusDays(offset);
        if (transactions.isEmpty()) {
            sendNoTransactions(chatId, date, messageId, locale);
        } else {
            sendTransactions(chatId, date, category, transactions, messageId, locale);
        }
    }

    private void sendNoTransactions(long chatId, LocalDate date, @Nullable Integer messageId, Locale locale) {
        String message = StringSubstitutor.replace(
            ResourceService.getValue("no-transactions-on-date", locale),
            Map.of("date", formatDate(date))
        );
        responseSender.sendResponse(chatId, messageId, null, message, false);
    }

    private void sendTransactions(
        long chatId,
        LocalDate date,
        Category category,
        List<Transaction> transactions,
        @Nullable Integer messageId,
        Locale locale
    ) {
        double total = TransactionUtil.sum(transactions);
        List<List<InlineKeyboardButton>> rows = getKeyboard(transactions);
        String messageTemplate = ResourceService.getValue("transactions-for-day-in-category", locale);
        Map<String, String> params = Map.of(
            "category", category.name(),
            "total", formatNumber(total),
            "date", formatDate(date)
        );
        String message = MarkdownUtil.escapeMarkdownV2(StringSubstitutor.replace(messageTemplate, params));
        responseSender.sendResponse(chatId, messageId, InlineKeyboardMarkup.builder().keyboard(rows).build(), message);
    }

    private List<List<InlineKeyboardButton>> getKeyboard(List<Transaction> transactions) {
        return IntStream.range(0, transactions.size())
            .mapToObj(index -> {
                Transaction tran = transactions.get(index);
                String text = "%d. %s : %s".formatted(
                    transactions.size() - index, tran.category(), formatNumber(tran.amount()));
                var callback = new Callback<>(
                    CallbackType.GET_TRANSACTION_ACTIONS,
                    new GetTransactionActionsCommand(tran.id())
                );
                UUID callbackId = callbackService.saveCallback(callback);
                return InlineKeyboardButton.builder()
                    .text(text)
                    .callbackData(callbackId.toString())
                    .build();
            })
            .map(List::of)
            .toList();
    }
}
