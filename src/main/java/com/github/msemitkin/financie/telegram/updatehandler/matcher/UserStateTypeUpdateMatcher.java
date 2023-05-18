package com.github.msemitkin.financie.telegram.updatehandler.matcher;

import com.github.msemitkin.financie.domain.User;
import com.github.msemitkin.financie.domain.UserService;
import com.github.msemitkin.financie.state.StateService;
import com.github.msemitkin.financie.state.StateType;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.github.msemitkin.financie.telegram.util.UpdateUtil.getSenderTelegramId;

class UserStateTypeUpdateMatcher implements UpdateMatcher {
    private final UserService userService;
    private final StateService stateService;
    private final StateType targetStateType;

    UserStateTypeUpdateMatcher(
        UserService userService,
        StateService stateService,
        StateType targetStateType
    ) {
        this.userService = userService;
        this.stateService = stateService;
        this.targetStateType = targetStateType;
    }

    @Override
    public boolean match(Update update) {
        long senderTelegramId = getSenderTelegramId(update);
        User user = userService.getUserByTelegramId(senderTelegramId);
        StateType stateType = stateService.getStateType(user.id());
        return stateType == targetStateType;
    }
}
