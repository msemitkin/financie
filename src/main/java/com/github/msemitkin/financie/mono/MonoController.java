package com.github.msemitkin.financie.mono;

import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.telegram.ResponseSender;
import com.github.msemitkin.financie.telegram.callback.Callback;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import com.github.msemitkin.financie.telegram.callback.command.SaveMonobankTransactionCommand;
import com.github.msemitkin.financie.telegram.util.ApplicationUrlProvider;
import com.github.msemitkin.financie.telegram.util.MarkdownUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.Optional;
import java.util.UUID;

@RestController
public class MonoController {
    private static final Logger logger = LoggerFactory.getLogger(MonoController.class);

    public static final String MONOBANK_CALLBACK_MAPPING = "/monobank/callback";
    private static final String MONOBANK_WEBHOOK_MAPPING = "/monobank/webhook";

    private final ResponseSender responseSender;
    private final MonobankService monobankService;
    private final UserService userService;
    private final ApplicationUrlProvider applicationUrlProvider;
    private final CallbackService callbackService;

    public MonoController(
        ResponseSender responseSender,
        MonobankService monobankService,
        UserService userService, ApplicationUrlProvider applicationUrlProvider, CallbackService callbackService
    ) {
        this.responseSender = responseSender;
        this.monobankService = monobankService;
        this.userService = userService;
        this.applicationUrlProvider = applicationUrlProvider;
        this.callbackService = callbackService;
    }

    @GetMapping(MONOBANK_CALLBACK_MAPPING + "/users/{userId}")
    public void handleUserCallback(@RequestHeader("X-Request-Id") String xRequestId, @PathVariable long userId) {
        logger.debug("Received callback from Monobank with X-Request-Id: {}", xRequestId);

        String userWebhookUrl = applicationUrlProvider.getApplicationRootUrl() + MONOBANK_WEBHOOK_MAPPING + "/users/" + userId;
        long telegramChatId = userService.getUserById(userId).telegramChatId().longValue();
        try {
            monobankService.registerWebHook(xRequestId, userWebhookUrl);
            responseSender.sendResponse(telegramChatId, null, null, "Webhook registration completed");
        } catch (Exception e) {
            responseSender.sendResponse(telegramChatId, null, null, "Error registering webhook");
        }
    }

    @GetMapping(MONOBANK_WEBHOOK_MAPPING + "/users/{userId}")
    public ResponseEntity<Void> onWebhookValidation(@PathVariable long userId) {
        logger.debug("Received webhook validation request for userId: {}", userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(MONOBANK_WEBHOOK_MAPPING + "/users/{userId}")
    public ResponseEntity<Void> onTransaction(@RequestBody Optional<WebhookEvent> event, @PathVariable long userId) {
        if (event.isEmpty()) {
            logger.debug("Received webhook validation request (POST) for userId: {}", userId);
            return ResponseEntity.ok().build();
        }
        logger.debug("Received event: {}", event);

        StatementItem tx = event.get().getData().getStatementItem();
        if (tx.getAmount() >= 0) {
            return ResponseEntity.ok().build();
        }

        long telegramChatId = userService.getUserById(userId).telegramChatId().longValue();
        double amount = -tx.getAmount() / 100.0;
        UUID callbackId = callbackService.saveCallback(new Callback<>(CallbackType.SAVE_MONOBANK_TRANSACTION, new SaveMonobankTransactionCommand(amount)));
        responseSender.sendResponse(telegramChatId, null, InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                    .text("Save")
                    .callbackData(callbackId.toString())
                    .build()))
                .build(),
            MarkdownUtil.escapeMarkdownV2(tx.getDescription() + ": " + amount));
        return ResponseEntity.ok().build();
    }
}
