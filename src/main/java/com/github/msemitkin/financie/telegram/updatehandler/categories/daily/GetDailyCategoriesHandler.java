package com.github.msemitkin.financie.telegram.updatehandler.categories.daily;

import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractQueryHandler;
import com.github.msemitkin.financie.telegram.updatehandler.categories.Response;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class GetDailyCategoriesHandler extends AbstractQueryHandler {
    private final TelegramApi telegramApi;
    private final DailyCategoriesResponseService dailyCategoriesResponseService;

    protected GetDailyCategoriesHandler(
        TelegramApi telegramApi,
        DailyCategoriesResponseService dailyCategoriesResponseService
    ) {
        super("daily_categories");
        this.telegramApi = telegramApi;
        this.dailyCategoriesResponseService = dailyCategoriesResponseService;
    }

    @Override
    public void handleUpdate(Update update) {
        Response response = dailyCategoriesResponseService.prepareResponse(update);

        telegramApi.execute(EditMessageText.builder()
            .chatId(getChatId(update))
            .messageId(update.getCallbackQuery().getMessage().getMessageId())
            .text(response.text())
            .parseMode(ParseMode.MARKDOWNV2)
            .replyMarkup(response.keyboardMarkup())
            .build());
    }
}
