package com.github.msemitkin.financie.telegram.updatehandler.categories.monthly;

import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.callback.command.GetMonthlyCategoriesCommand;
import com.github.msemitkin.financie.telegram.updatehandler.AbstractTextCommandHandler;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;

@Component
public class GetThisMonthCategoriesHandler extends AbstractTextCommandHandler {
    private final TelegramApi telegramApi;
    private final MonthlyCategoriesResponseService monthlyCategoriesResponseService;

    public GetThisMonthCategoriesHandler(
        TelegramApi telegramApi,
        MonthlyCategoriesResponseService monthlyCategoriesResponseService
    ) {
        super(ResourceService.getValues("button.this-month"));
        this.telegramApi = telegramApi;
        this.monthlyCategoriesResponseService = monthlyCategoriesResponseService;
    }

    @Override
    public void handleUpdate(Update update) {
        var response = monthlyCategoriesResponseService.getResponse(update, new GetMonthlyCategoriesCommand(0));
        Long chatId = getChatId(update);
        sendMessage(chatId, response.text(), response.keyboardMarkup());
    }

    private void sendMessage(
        Long chatId,
        String text,
        @Nullable ReplyKeyboard replyKeyboard
    ) {
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .parseMode(ParseMode.MARKDOWNV2)
            .replyMarkup(replyKeyboard)
            .build();
        telegramApi.execute(sendMessage);
    }
}
