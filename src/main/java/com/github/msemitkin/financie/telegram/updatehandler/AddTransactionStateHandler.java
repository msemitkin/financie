package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.SaveTransactionCommand;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.domain.TransactionValidationException;
import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.state.AddTransactionContext;
import com.github.msemitkin.financie.state.StateService;
import com.github.msemitkin.financie.state.StateType;
import com.github.msemitkin.financie.telegram.MessageException;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContext;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.keyboard.KeyboardService;
import com.github.msemitkin.financie.telegram.transaction.IncomingTransaction;
import com.github.msemitkin.financie.telegram.transaction.TransactionCommandValidator;
import com.github.msemitkin.financie.telegram.transaction.TransactionParser;
import com.github.msemitkin.financie.telegram.transaction.TransactionRecognizer;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import com.github.msemitkin.financie.telegram.util.MarkdownUtil;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getMessage;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;

@Component
public class AddTransactionStateHandler extends BaseUpdateHandler {
    private final UserService userService;
    private final StateService stateService;
    private final TransactionRecognizer transactionRecognizer;
    private final TelegramApi telegramApi;
    private final TransactionParser transactionParser;
    private final TransactionService transactionService;
    private final TransactionCommandValidator transactionCommandValidator;
    private final KeyboardService keyboardService;

    private final Set<String> cancelCommands = ResourceService.getValues("button.cancel");

    protected AddTransactionStateHandler(
        UserService userService,
        StateService stateService,
        TransactionRecognizer transactionRecognizer,
        TelegramApi telegramApi,
        TransactionParser transactionParser,
        TransactionService transactionService,
        TransactionCommandValidator transactionCommandValidator,
        KeyboardService keyboardService
    ) {
        super(UpdateMatcher.userStateTypeUpdateMatcher(userService, stateService, StateType.ADD_TRANSACTION));
        this.userService = userService;
        this.stateService = stateService;
        this.transactionRecognizer = transactionRecognizer;
        this.telegramApi = telegramApi;
        this.transactionParser = transactionParser;
        this.transactionService = transactionService;
        this.transactionCommandValidator = transactionCommandValidator;
        this.keyboardService = keyboardService;
    }

    @Override
    protected void handleUpdate(Update update) {
        String message = getMessage(update);
        UserContext context = UserContextHolder.getContext();
        ZoneId zoneId = context.timeZone().toZoneId();
        User user = userService.getUserByTelegramId(getSenderTelegramId(update));
        if (message != null && transactionRecognizer.hasTransactionFormat(message)) {
            try {
                transactionCommandValidator.validateTransaction(message);

                var state = stateService.getCurrentState(user.id(), AddTransactionContext.class);
                IncomingTransaction incomingTransaction = transactionParser.parseTransaction(message);
                var transactionToSave = prepareTransactionToSave(user.id(), state.context(), incomingTransaction, zoneId);

                transactionService.saveTransaction(transactionToSave);
                StateType nextState = StateType.IDLE;
                LocalDate transactionDate = state.context().forDate();
                String replyText = StringSubstitutor.replace(
                    ResourceService.getValue("transaction-saved-on-date-reply", context.locale()),
                    Map.of("date", transactionDate));
                telegramApi.execute(SendMessage.builder()
                    .chatId(getChatId(update))
                    .replyMarkup(keyboardService.getKeyboardForState(nextState, context.locale()))
                    .text(replyText)
                    .build());
                stateService.setStateType(user.id(), nextState);
            } catch (MessageException | TransactionValidationException e) {
                sendErrorMessage(update, context.locale());
            }
        } else if (cancelCommands.contains(message)) {
            StateType nextState = StateType.IDLE;
            telegramApi.execute(SendMessage.builder()
                .chatId(getChatId(update))
                .text(message)
                .replyMarkup(keyboardService.getKeyboardForState(nextState, context.locale()))
                .build());
            stateService.setStateType(user.id(), nextState);
        } else {
            sendErrorMessage(update, context.locale());
        }
    }

    @NonNull
    private SaveTransactionCommand prepareTransactionToSave(
        long userId,
        AddTransactionContext context,
        IncomingTransaction incomingTransaction,
        ZoneId zoneId
    ) {
        LocalDateTime transactionTime = context.forDate()
            .atStartOfDay()
            .atZone(zoneId)
            .withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime();
        return new SaveTransactionCommand(
            userId,
            incomingTransaction.amount(),
            incomingTransaction.category(),
            null,
            transactionTime
        );
    }

    private void sendErrorMessage(Update update, Locale locale) {
        String text = MarkdownUtil.escapeMarkdownV2(ResourceService.getValue(
            "message.use-correct-transaction-format", locale));
        telegramApi.execute(SendMessage.builder()
            .chatId(getChatId(update))
            .text(text)
            .parseMode(ParseMode.MARKDOWNV2)
            .build());
    }
}
