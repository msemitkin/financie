package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.state.AddTransactionContext;
import com.github.msemitkin.financie.state.StateService;
import com.github.msemitkin.financie.state.StateType;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.callback.CallbackDataExtractor;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.AddTransactionCommand;
import com.github.msemitkin.financie.telegram.keyboard.KeyboardService;
import com.github.msemitkin.financie.telegram.updatehandler.BaseUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import com.github.msemitkin.financie.telegram.util.UpdateUtil;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;
import java.util.Map;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class AddTransactionCallbackHandler extends BaseUpdateHandler {
    private final CallbackDataExtractor callbackDataExtractor;
    private final StateService stateService;
    private final UserService userService;
    private final KeyboardService keyboardService;
    private final TelegramApi telegramApi;

    protected AddTransactionCallbackHandler(
        CallbackService callbackService,
        CallbackDataExtractor callbackDataExtractor,
        StateService stateService,
        UserService userService,
        KeyboardService keyboardService,
        TelegramApi telegramApi
    ) {
        super(UpdateMatcher.callbackQueryMatcher(callbackService, CallbackType.ADD_TRANSACTION));
        this.callbackDataExtractor = callbackDataExtractor;
        this.stateService = stateService;
        this.userService = userService;
        this.keyboardService = keyboardService;
        this.telegramApi = telegramApi;
    }

    @Override
    public void handleUpdate(Update update) {
        Locale locale = UserContextHolder.getContext().locale();
        long senderTelegramId = UpdateUtil.getSenderTelegramId(update);
        User user = userService.getUserByTelegramId(senderTelegramId);
        AddTransactionCommand callbackData = callbackDataExtractor.getCallbackData(update, AddTransactionCommand.class);

        StateType nextState = StateType.ADD_TRANSACTION;
        var replyMarkup = keyboardService.getKeyboardForState(nextState, locale);
        String message = StringSubstitutor.replace(
            ResourceService.getValue("message.add-new-transaction", locale),
            Map.of("date", callbackData.date()));
        telegramApi.execute(SendMessage.builder()
            .chatId(getChatId(update))
            .text(message)
            .replyMarkup(replyMarkup)
            .build());
        stateService.setState(user.id(), nextState, new AddTransactionContext(callbackData.date()));
    }
}
