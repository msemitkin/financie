package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.github.msemitkin.financie.telegram.util.FormatterUtil.formatDate;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;
import static java.util.Objects.requireNonNull;

@Component
public class GetMonthlyCategoryStatisticsUpdateHandler implements UpdateHandler {
    private final AbsSender absSender;
    private final TransactionService transactionService;
    private final StatisticsService statisticsService;

    public GetMonthlyCategoryStatisticsUpdateHandler(
        AbsSender absSender, TransactionService transactionService,
        StatisticsService statisticsService
    ) {
        this.absSender = absSender;
        this.transactionService = transactionService;
        this.statisticsService = statisticsService;
    }

    @Override
    public boolean canHandle(Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
            .map(CallbackQuery::getData)
            .map(callbackData -> new Gson().fromJson(callbackData, JsonObject.class))
            .map(json -> json.get("type").getAsString())
            .map("monthly_stats"::equals)
            .orElse(false);
    }

    @Override
    public void handleUpdate(Update update) {
        JsonObject jsonObject = new Gson().fromJson(update.getCallbackQuery().getData(), JsonObject.class);
        String category = jsonObject.get("category").getAsString();
        Long telegramUserId = requireNonNull(getSenderTelegramId(update));
        long userId = transactionService.getOrCreateUserByTelegramId(telegramUserId);
        List<Transaction> transactionsInCategory = statisticsService
            .getMonthlyCategoryStatistics(userId, category);

        String header = LocalDate.now().getMonth().toString().concat(" statistics");
        String message = transactionsInCategory.stream()
            .reduce(new StringBuilder(header),
                (builder, curr) -> {
                    String text = "%n%s : %.1f".formatted(formatDate(curr.time().toLocalDate()), curr.amount());
                    return builder.append(text);
                }, StringBuilder::append)
            .toString();
        sendMessage(getChatId(update), message);
        //TODO edit existing message and markup instead of sending a new one
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .build();
        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
