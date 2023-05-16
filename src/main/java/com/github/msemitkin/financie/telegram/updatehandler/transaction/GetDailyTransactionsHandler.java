package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.Category;
import com.github.msemitkin.financie.domain.CategoryService;
import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.domain.TransactionUtil;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.callback.Callback;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.GetDailyCategoryTransactionsCommand;
import com.github.msemitkin.financie.telegram.callback.command.GetTransactionActionsCommand;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractQueryHandler;
import com.github.msemitkin.financie.telegram.util.MarkdownUtil;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatDate;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getFrom;

@Component
public class GetDailyTransactionsHandler extends AbstractQueryHandler {
    private final TransactionService transactionService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TelegramApi telegramApi;

    public GetDailyTransactionsHandler(
        TransactionService transactionService,
        UserService userService,
        CategoryService categoryService,
        CallbackService callbackService,
        TelegramApi telegramApi
    ) {
        super(CallbackType.GET_CATEGORY_TRANSACTIONS_FOR_DAY, callbackService);
        this.transactionService = transactionService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.telegramApi = telegramApi;
    }

    @Override
    public void handleUpdate(Update update) {
        long chatId = getChatId(update);
        long userTelegramId = getFrom(update).getId();
        long userId = userService.getUserByTelegramId(userTelegramId).id();
        var callbackData = getCallbackData(update, GetDailyCategoryTransactionsCommand.class);
        long categoryId = callbackData.categoryId();
        int offset = callbackData.offset();
        LocalDateTime startOfDay = LocalDate.now().plusDays(offset).atStartOfDay();
        LocalDateTime startOfNextDay = startOfDay.plusDays(1);
        Category category = categoryService.getCategory(categoryId);
        List<Transaction> transactions = transactionService
            .getTransactions(userId, category.name(), startOfDay, startOfNextDay)
            .stream()
            .sorted(Comparator.comparing(Transaction::time).reversed())
            .toList();

        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        Locale locale = UserContextHolder.getContext().locale();

        if (transactions.isEmpty()) {
            sendNoTransactions(chatId, messageId, locale);
        } else {
            sendTransactions(chatId, startOfDay.toLocalDate(), category, transactions, messageId, locale);
        }
    }

    private void sendNoTransactions(long chatId, Integer messageId, Locale locale) {
        String message = StringSubstitutor.replace(
            ResourceService.getValue("no-transactions-on-date", locale),
            Map.of("date", formatDate(LocalDate.now()))
        );
        reply(chatId, messageId, Collections.emptyList(), message);
    }

    private void sendTransactions(
        long chatId,
        LocalDate date,
        Category category,
        List<Transaction> transactions,
        Integer messageId,
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
        reply(chatId, messageId, rows, message);
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

    private void reply(
        long chatId,
        int messageId,
        List<List<InlineKeyboardButton>> rows,
        String message
    ) {
        telegramApi.execute(EditMessageText.builder()
            .chatId(chatId)
            .messageId(messageId)
            .text(message)
            .parseMode(ParseMode.MARKDOWNV2)
            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rows).build())
            .build());
    }
}
