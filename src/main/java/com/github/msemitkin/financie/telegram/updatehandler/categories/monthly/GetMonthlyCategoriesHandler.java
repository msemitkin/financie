package com.github.msemitkin.financie.telegram.updatehandler.categories.monthly;

import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.callback.CallbackDataExtractor;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.GetMonthlyCategoriesCommand;
import com.github.msemitkin.financie.telegram.updatehandler.BaseUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.categories.Response;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class GetMonthlyCategoriesHandler extends BaseUpdateHandler {
    private final TelegramApi telegramApi;
    private final MonthlyCategoriesResponseService monthlyCategoriesResponseService;
    private final CallbackDataExtractor callbackDataExtractor;

    public GetMonthlyCategoriesHandler(
        TelegramApi telegramApi,
        MonthlyCategoriesResponseService monthlyCategoriesResponseService,
        CallbackService callbackService,
        CallbackDataExtractor callbackDataExtractor) {
        super(UpdateMatcher.callbackQueryMatcher(callbackService, CallbackType.GET_CATEGORIES_FOR_MONTH));
        this.telegramApi = telegramApi;
        this.monthlyCategoriesResponseService = monthlyCategoriesResponseService;
        this.callbackDataExtractor = callbackDataExtractor;
    }

    @Override
    public void handleUpdate(Update update) {
        var callbackData = callbackDataExtractor.getCallbackData(update, GetMonthlyCategoriesCommand.class);
        Response response = monthlyCategoriesResponseService.getResponse(update, callbackData);
        long chatId = getChatId(update);
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        telegramApi.execute(EditMessageText.builder()
            .chatId(chatId)
            .messageId(messageId)
            .text(response.text())
            .parseMode(ParseMode.MARKDOWNV2)
            .replyMarkup(response.keyboardMarkup())
            .build());
    }
}
