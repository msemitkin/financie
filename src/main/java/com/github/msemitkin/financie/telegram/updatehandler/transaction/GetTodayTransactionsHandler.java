package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.domain.TransactionUtil;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractQueryHandler;
import com.github.msemitkin.financie.telegram.util.JsonUtil;
import com.google.gson.JsonObject;
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
import java.util.Map;
import java.util.stream.IntStream;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatNumber;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;

@Component
public class GetTodayTransactionsHandler extends AbstractQueryHandler {
    private final TransactionService transactionService;
    private final UserService userService;
    private final TelegramApi telegramApi;

    public GetTodayTransactionsHandler(
        TransactionService transactionService,
        UserService userService,
        TelegramApi telegramApi
    ) {
        super("day_trans");
        this.transactionService = transactionService;
        this.userService = userService;
        this.telegramApi = telegramApi;
    }

    @Override
    public void handleUpdate(Update update) {
        long senderTelegramId = getSenderTelegramId(update);
        long userId = userService.getOrCreateUserByTelegramId(senderTelegramId);
        long chatId = getChatId(update);
        JsonObject payload = getCallbackData(update);
        String category = payload.get("category").getAsString();
        List<Transaction> transactions = transactionService
            .getTransactions(userId, category, LocalDate.now().atStartOfDay(), LocalDateTime.now())
            .stream()
            .sorted(Comparator.comparing(Transaction::time).reversed())
            .toList();

        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        if (transactions.isEmpty()) {
            telegramApi.execute(EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text("No transactions today")
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(Collections.emptyList()).build())
                .build());
        } else {
            double total = TransactionUtil.sum(transactions);
            List<List<InlineKeyboardButton>> rows = getKeyboard(transactions);
            telegramApi.execute(EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text("Today in category *%s*: `%s`".formatted(category, formatNumber(total)))
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rows).build())
                .build());
        }
    }

    private List<List<InlineKeyboardButton>> getKeyboard(List<Transaction> transactions) {
        return IntStream.range(0, transactions.size())
            .mapToObj(index -> {
                Transaction tran = transactions.get(index);
                return InlineKeyboardButton.builder()
                    .text("%d. %s : %s".formatted(transactions.size() - index, tran.category(), formatNumber(tran.amount())))
                    .callbackData(JsonUtil.toJson(Map.of("type", "transactions/actions", "transactionId", tran.id())))
                    .build();
            })
            .map(List::of)
            .toList();
    }

}
