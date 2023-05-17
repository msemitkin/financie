package com.github.msemitkin.financie.telegram.keyboard;

import com.github.msemitkin.financie.resources.ResourceService;
import com.github.msemitkin.financie.state.StateType;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Locale;

import static com.github.msemitkin.financie.telegram.util.KeyboardUtil.keyboard;
import static com.github.msemitkin.financie.telegram.util.KeyboardUtil.requestLocationButton;
import static com.github.msemitkin.financie.telegram.util.KeyboardUtil.row;
import static com.github.msemitkin.financie.telegram.util.KeyboardUtil.textButton;

@Service
public class KeyboardService {

    public ReplyKeyboardMarkup getKeyboardForState(StateType stateType, Locale locale) {
        return switch (stateType) {
            case NONE, IDLE -> keyboard(
                row(
                    textButton(ResourceService.getValue("button.today", locale)),
                    textButton(ResourceService.getValue("button.this-month", locale))
                ),
                row(textButton(ResourceService.getValue("button.menu", locale)))
            );
            case MENU -> keyboard(
                row(textButton(ResourceService.getValue("button.import", locale))),
                row(textButton(ResourceService.getValue("button.settings", locale))),
                row(textButton(ResourceService.getValue("button.back", locale)))
            );
            case SETTINGS -> keyboard(
                row(requestLocationButton(ResourceService.getValue("button.change-timezone", locale))),
                row(textButton(ResourceService.getValue("button.back", locale)))
            );
            case IMPORT -> keyboard(
                row(textButton(ResourceService.getValue("button.cancel", locale)))
            );
        };
    }
}
