package com.github.msemitkin.financie.telegram.updatehandler.categories.daily;

import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractTextCommandHandler;
import com.github.msemitkin.financie.telegram.updatehandler.categories.Response;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class GetTodayCategoriesHandler extends AbstractTextCommandHandler {
    private final TelegramApi telegramApi;
    private final DailyCategoriesResponseService dailyCategoriesResponseService;

    protected GetTodayCategoriesHandler(
        TelegramApi telegramApi,
        DailyCategoriesResponseService dailyCategoriesResponseService
    ) {
        super(BotCommand.TODAY.getCommand());
        this.telegramApi = telegramApi;
        this.dailyCategoriesResponseService = dailyCategoriesResponseService;
    }

    @Override
    public void handleUpdate(Update update) {
        Response response = dailyCategoriesResponseService.prepareResponse(update);

        telegramApi.execute(SendMessage.builder()
            .chatId(getChatId(update))
            .text(response.text())
            .parseMode(ParseMode.MARKDOWNV2)
            .replyMarkup(response.keyboardMarkup())
            .build());
    }
}
