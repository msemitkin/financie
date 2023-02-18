package com.github.msemitkin.financie.telegram.updatehandler;

import com.github.msemitkin.financie.telegram.api.TelegramApi;
import com.github.msemitkin.financie.telegram.command.BotCommand;
import com.github.msemitkin.financie.telegram.util.MarkdownUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getChatId;
import static java.util.Objects.requireNonNull;

@Component
public class HelpHandler extends AbstractTextCommandHandler {
    private final TelegramApi telegramApi;

    public HelpHandler(TelegramApi telegramApi) {
        super(BotCommand.HELP.getCommand());
        this.telegramApi = telegramApi;
    }

    @Override
    public void handleUpdate(Update update) {
        long chatId = requireNonNull(getChatId(update));
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .parseMode(ParseMode.MARKDOWNV2)
            .text(MarkdownUtil.escapeMarkdownV2(
                """
                    Welcome to the financie — spending tracker bot!
                    This bot allows you to keep track of your expenses by recording your spending in the following format:
                                  
                    <amount> <category>
                                  
                    For example, to record that you spent 500 on a car, you would send the following message:
                                  
                    `500 car`
                                  
                    The bot will automatically record the amount and category of the spending, along with the date and time.
                                    
                    That's it! With this bot, you can keep track of your spending and make better financial decisions.
                    """
            ))
            .build();
        telegramApi.execute(sendMessage);
    }
}
