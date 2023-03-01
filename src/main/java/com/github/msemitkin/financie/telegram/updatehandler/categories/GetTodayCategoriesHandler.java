package com.github.msemitkin.financie.telegram.updatehandler.categories;

import com.github.msemitkin.financie.domain.StatisticsService;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractTextCommandHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class GetTodayCategoriesHandler extends AbstractTextCommandHandler {
    private final UserService userService;
    private final StatisticsService statisticsService;
    private final TelegramApi telegramApi;

    protected GetTodayCategoriesHandler(
        UserService userService,
        StatisticsService statisticsService,
        TelegramApi telegramApi
    ) {
        super(BotCommand.TODAY.getCommand());
        this.userService = userService;
        this.statisticsService = statisticsService;
        this.telegramApi = telegramApi;
    }

    @Override
    public void handleUpdate(Update update) {
        ResponseSender responseSender = (text, keyboardMarkup) -> telegramApi
            .execute(SendMessage.builder()
                .chatId(getChatId(update))
                .text(text)
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(keyboardMarkup)
                .build());
        new GetDailyCategoriesService(update, responseSender, statisticsService, userService).process();
    }
}
