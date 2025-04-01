package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.domain.SaveTransactionCommand;
import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.state.EnterTransactionCategoryContext;
import com.github.msemitkin.financie.state.StateService;
import com.github.msemitkin.financie.state.StateType;
import com.github.msemitkin.financie.state.UserState;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContext;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.keyboard.KeyboardService;
import com.github.msemitkin.financie.telegram.updatehandler.common.SendSuccessfullySavedTransactionService;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Locale;
import java.util.Set;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getMessage;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;

@Component
public class EnterTransactionCategoryStateHandler extends BaseUpdateHandler {
    private static final Set<String> CANCEL_BUTTON_COMMANDS = ResourceService.getValues("button.cancel");

    private final StateService stateService;
    private final TelegramApi telegramApi;
    private final KeyboardService keyboardService;
    private final TransactionService transactionService;
    private final UserService userService;
    private final SendSuccessfullySavedTransactionService sendSuccessfullySavedTransactionService;

    protected EnterTransactionCategoryStateHandler(
        UserService userService,
        StateService stateService,
        TelegramApi telegramApi,
        KeyboardService keyboardService,
        TransactionService transactionService,
        SendSuccessfullySavedTransactionService sendSuccessfullySavedTransactionService
    ) {
        super(UpdateMatcher.userStateTypeUpdateMatcher(userService, stateService, StateType.ENTER_TRANSACTION_CATEGORY));
        this.stateService = stateService;
        this.telegramApi = telegramApi;
        this.keyboardService = keyboardService;
        this.transactionService = transactionService;
        this.userService = userService;
        this.sendSuccessfullySavedTransactionService = sendSuccessfullySavedTransactionService;
    }

    @Override
    protected void handleUpdate(Update update) {
        UserContext userContext = UserContextHolder.getContext();
        Locale userLocale = userContext.locale();
        long senderTelegramId = getSenderTelegramId(update);
        User user = userService.getUserByTelegramId(senderTelegramId);

        String incomingMessage = getMessage(update);
        StateType nextState = StateType.IDLE;
        ReplyKeyboardMarkup nextKeyboard = keyboardService.getKeyboardForState(nextState, userLocale);
        if (incomingMessage != null && CANCEL_BUTTON_COMMANDS.contains(incomingMessage)) {
            stateService.setStateType(user.id(), nextState);
            telegramApi.execute(SendMessage.builder()
                .chatId(getChatId(update))
                .text(incomingMessage)
                .replyMarkup(nextKeyboard)
                .build());
        } else if (incomingMessage != null) {
            UserState<EnterTransactionCategoryContext> userState = stateService
                .getCurrentState(user.id(), EnterTransactionCategoryContext.class);
            double amount = userState.context().amount();
            transactionService.saveTransaction(new SaveTransactionCommand(user.id(), amount, incomingMessage, null, null));
            stateService.setStateType(user.id(), nextState);
            sendSuccessfullySavedTransactionService.sendSuccessfullySavedTransaction(
                user.telegramChatId(),
                user.id(),
                update.getMessage().getMessageId(),
                incomingMessage,
                userLocale,
                userContext.timeZone().toZoneId(),
                nextKeyboard
            );
        }
    }
}
