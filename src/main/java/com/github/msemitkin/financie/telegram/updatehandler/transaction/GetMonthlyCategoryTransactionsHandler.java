package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.Category;
import com.github.msemitkin.financie.domain.CategoryService;
import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.callback.Callback;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.DeleteTransactionCommand;
import com.github.msemitkin.financie.telegram.callback.command.GetMonthlyCategoryTransactionsCommand;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractQueryHandler;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatMonth;
import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.telegram.util.MarkdownUtil.escapeMarkdownV2;
import static com.github.msemitkin.financie.telegram.util.TransactionUtil.getTransactionRepresentation;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getFrom;

@Component
public class GetMonthlyCategoryTransactionsHandler extends AbstractQueryHandler {
    private final UserService userService;
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final TelegramApi telegramApi;
    private final int maxNumberOfStatisticsRecords;

    public GetMonthlyCategoryTransactionsHandler(
        UserService userService,
        TransactionService transactionService,
        CategoryService categoryService,
        CallbackService callbackService,
        TelegramApi telegramApi,
        @Value("${com.github.msemitkin.financie.statistics.max-number-of-displayed-records}")
        int maxNumberOfStatisticsRecords
    ) {
        super(CallbackType.GET_CATEGORY_TRANSACTIONS_FOR_MONTH, callbackService);
        this.userService = userService;
        this.transactionService = transactionService;
        this.categoryService = categoryService;
        this.telegramApi = telegramApi;
        this.maxNumberOfStatisticsRecords = maxNumberOfStatisticsRecords;
    }

    @Override
    public void handleUpdate(Update update) {
        var callbackData = getCallbackData(update, GetMonthlyCategoryTransactionsCommand.class);
        long categoryId = callbackData.categoryId();
        int offset = callbackData.offset();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        long userTelegramId = getFrom(update).getId();
        long chatId = getChatId(update);
        long userId = userService.getUserByTelegramId(userTelegramId).id();
        LocalDateTime startOfMonth = YearMonth.now().plusMonths(offset).atDay(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        Category category = categoryService.getCategory(categoryId);

        List<Transaction> transactionsInCategory = transactionService
            .getTransactions(userId, category.name(), startOfMonth, startOfNextMonth)
            .stream().sorted(Comparator.comparing(Transaction::time).reversed()).toList();

        Double totalInCategory = transactionsInCategory.stream()
            .reduce(0.0, (res, tran) -> res + tran.amount(), Double::sum);

        String message = getMessage(category.name(), totalInCategory);
        InlineKeyboardMarkup keyboard = getKeyboardMarkup(transactionsInCategory);
        editMessage(chatId, messageId, message, keyboard);
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
            .map(transaction -> {
                var callback = new Callback<>(
                    CallbackType.DELETE_TRANSACTION,
                    new DeleteTransactionCommand(transaction.id())
                );
                UUID deleteTransactionCallbackId = callbackService.saveCallback(callback);
                return List.of(
                    InlineKeyboardButton.builder()
                        .text(getTransactionRepresentation(transaction))
                        .callbackData("-1")
                        .build(),
                    InlineKeyboardButton.builder()
                        .text("❌")
                        .callbackData(deleteTransactionCallbackId.toString())
                        .build()
                );
            })
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
