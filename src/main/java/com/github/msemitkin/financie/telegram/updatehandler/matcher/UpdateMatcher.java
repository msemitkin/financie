package com.github.msemitkin.financie.telegram.updatehandler.matcher;

import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.state.StateService;
import com.github.msemitkin.financie.state.StateType;
import com.github.msemitkin.financie.telegram.callback.CallbackService;
import com.github.msemitkin.financie.telegram.callback.CallbackType;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;

public interface UpdateMatcher {

    static UpdateMatcher textCommandMatcher(String command) {
        return textCommandMatcher(Set.of(command));
    }

    static UpdateMatcher textCommandMatcher(Set<String> commands) {
        return new TextCommandUpdateMatcher(commands);
    }

    static UpdateMatcher callbackQueryMatcher(CallbackService callbackService, CallbackType callbackType) {
        return callbackQueryMatcher(callbackService, Set.of(callbackType));
    }

    static UpdateMatcher callbackQueryMatcher(CallbackService callbackService, Set<CallbackType> callbackTypes) {
        return new CallbackQueryUpdateMatcher(callbackService, callbackTypes);
    }

    static UpdateMatcher userStateTypeUpdateMatcher(
        UserService userService,
        StateService stateService,
        StateType targetState
    ) {
        return new UserStateTypeUpdateMatcher(userService, stateService, targetState);
    }

    boolean match(Update update);

}
