package com.github.msemitkin.financie.telegram.updatehandler.categories.daily;

import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.callback.command.GetDailyCategoriesCommand;
import com.github.msemitkin.financie.telegram.updatehandler.BaseUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class GetTodayCategoriesHandler extends BaseUpdateHandler {
    private final TelegramApi telegramApi;
    private final DailyCategoriesResponseService dailyCategoriesResponseService;

    protected GetTodayCategoriesHandler(
        TelegramApi telegramApi,
        DailyCategoriesResponseService dailyCategoriesResponseService
    ) {
        super(UpdateMatcher.textCommandMatcher(ResourceService.getValues("button.today")));
        this.telegramApi = telegramApi;
        this.dailyCategoriesResponseService = dailyCategoriesResponseService;
    }

    @Override
    public void handleUpdate(Update update) {
        var response = dailyCategoriesResponseService.prepareResponse(update, new GetDailyCategoriesCommand(0));

        telegramApi.execute(SendMessage.builder()
            .chatId(getChatId(update))
            .text(response.text())
            .parseMode(ParseMode.MARKDOWNV2)
            .replyMarkup(response.keyboardMarkup())
            .build());
    }
}
