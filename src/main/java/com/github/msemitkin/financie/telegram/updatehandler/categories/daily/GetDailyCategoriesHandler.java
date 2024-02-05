package com.github.msemitkin.financie.telegram.updatehandler.categories.daily;

import com.github.msemitkin.financie.telegram.ResponseSender;
import com.github.msemitkin.financie.telegram.callback.CallbackDataExtractor;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.GetDailyCategoriesCommand;
import com.github.msemitkin.financie.telegram.updatehandler.BaseUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.categories.Response;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getAccessibleMessageId;
import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class GetDailyCategoriesHandler extends BaseUpdateHandler {
    private final ResponseSender responseSender;
    private final DailyCategoriesResponseService dailyCategoriesResponseService;
    private final CallbackDataExtractor callbackDataExtractor;

    protected GetDailyCategoriesHandler(
        ResponseSender responseSender,
        DailyCategoriesResponseService dailyCategoriesResponseService,
        CallbackService callbackService,
        CallbackDataExtractor callbackDataExtractor) {
        super(UpdateMatcher.callbackQueryMatcher(callbackService, CallbackType.GET_CATEGORIES_FOR_DAY));
        this.responseSender = responseSender;
        this.dailyCategoriesResponseService = dailyCategoriesResponseService;
        this.callbackDataExtractor = callbackDataExtractor;
    }

    @Override
    public void handleUpdate(Update update) {
        var callbackData = callbackDataExtractor.getCallbackData(update, GetDailyCategoriesCommand.class);
        Response response = dailyCategoriesResponseService.prepareResponse(update, callbackData);

        Integer messageId = getAccessibleMessageId(update.getCallbackQuery().getMessage());

        responseSender.sendResponse(getChatId(update), messageId, response.keyboardMarkup(), response.text());
    }
}
