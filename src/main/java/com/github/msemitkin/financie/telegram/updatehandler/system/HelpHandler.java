package com.github.msemitkin.financie.telegram.updatehandler.system;

import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.auth.UserContextHolder;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import com.github.msemitkin.financie.telegram.updatehandler.BaseUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.matcher.UpdateMatcher;
import com.github.msemitkin.financie.telegram.util.MarkdownUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static java.util.Objects.requireNonNull;

@Component
public class HelpHandler extends BaseUpdateHandler {
    private final TelegramApi telegramApi;

    public HelpHandler(TelegramApi telegramApi) {
        super(UpdateMatcher.textCommandMatcher(BotCommand.HELP.getCommand()));
        this.telegramApi = telegramApi;
    }


    @Override
    protected void handleUpdate(Update update) {
        long chatId = requireNonNull(getChatId(update));
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .parseMode(ParseMode.MARKDOWNV2)
            .text(MarkdownUtil.escapeMarkdownV2(
                ResourceService.getValue("help-message", UserContextHolder.getContext().locale())))
            .build();
        telegramApi.execute(sendMessage);
    }
}
