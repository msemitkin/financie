package com.github.msemitkin.financie.telegram.updatehandler.transaction;

import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.state.EnterTransactionCategoryContext;
import com.github.msemitkin.financie.state.StateService;
import com.github.msemitkin.financie.state.StateType;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContext;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.callback.CallbackDataExtractor;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.SaveMonobankTransactionCommand;
import com.github.msemitkin.financie.telegram.keyboard.KeyboardService;
import com.github.msemitkin.financie.telegram.updatehandler.BaseUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import com.github.msemitkin.financie.telegram.util.UpdateUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Locale;

@Component
public class SaveMonobankTransactionCallbackHandler extends BaseUpdateHandler {
    private final CallbackDataExtractor callbackDataExtractor;
    private final StateService stateService;
    private final UserService userService;
    private final KeyboardService keyboardService;
    private final TelegramApi telegramApi;

    protected SaveMonobankTransactionCallbackHandler(
        CallbackService callbackService,
        CallbackDataExtractor callbackDataExtractor, StateService stateService, UserService userService, KeyboardService keyboardService, TelegramApi telegramApi
    ) {
        super(UpdateMatcher.callbackQueryMatcher(callbackService, CallbackType.SAVE_MONOBANK_TRANSACTION));
        this.callbackDataExtractor = callbackDataExtractor;
        this.stateService = stateService;
        this.userService = userService;
        this.keyboardService = keyboardService;
        this.telegramApi = telegramApi;
    }

    @Override
    protected void handleUpdate(Update update) {
        long senderTelegramId = UpdateUtil.getSenderTelegramId(update);
        User user = userService.getUserByTelegramId(senderTelegramId);
        SaveMonobankTransactionCommand callbackData = callbackDataExtractor.getCallbackData(update, SaveMonobankTransactionCommand.class);
        stateService.setState(user.id(), StateType.ENTER_TRANSACTION_CATEGORY, new EnterTransactionCategoryContext(callbackData.amount()));
        UserContext userContext = UserContextHolder.getContext();
        Locale userLocale = userContext.locale();
        ReplyKeyboardMarkup keyboardForState = keyboardService.getKeyboardForState(StateType.ENTER_TRANSACTION_CATEGORY, userLocale);
        String message = ResourceService.getValue("message.enter-transaction-category", userLocale);
        telegramApi.execute(SendMessage.builder()
            .chatId(user.telegramChatId())
            .text(message)
            .replyMarkup(keyboardForState)
            .build());
    }
}
