package com.github.msemitkin.financie.telegram.updatehandler.categories.monthly;

import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractQueryHandler;
import com.github.msemitkin.financie.telegram.updatehandler.categories.Response;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class GetMonthlyCategoriesHandler extends AbstractQueryHandler {
    private final TelegramApi telegramApi;
    private final MonthlyCategoriesResponseService monthlyCategoriesResponseService;

    public GetMonthlyCategoriesHandler(
        TelegramApi telegramApi,
        MonthlyCategoriesResponseService monthlyCategoriesResponseService
    ) {
        super("monthly_categories");
        this.telegramApi = telegramApi;
        this.monthlyCategoriesResponseService = monthlyCategoriesResponseService;
    }

    @Override
    public void handleUpdate(Update update) {
        Response response = monthlyCategoriesResponseService.getResponse(update);
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
