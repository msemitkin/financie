package com.github.msemitkin.financie.telegram.updatehandler.chain;

import com.github.msemitkin.financie.telegram.updatehandler.DefaultUpdateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.ImportStateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.MenuStateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.SettingsStateHandler;
import com.github.msemitkin.financie.telegram.updatehandler.categories.daily.GetDailyCategoriesHandler;
import com.github.msemitkin.financie.telegram.updatehandler.categories.daily.GetTodayCategoriesHandler;
import com.github.msemitkin.financie.telegram.updatehandler.categories.monthly.GetMonthlyCategoriesHandler;
import com.github.msemitkin.financie.telegram.updatehandler.categories.monthly.GetThisMonthCategoriesHandler;
import com.github.msemitkin.financie.telegram.updatehandler.system.AuthorHandler;
import com.github.msemitkin.financie.telegram.updatehandler.system.HelpHandler;
import com.github.msemitkin.financie.telegram.updatehandler.system.MenuHandler;
import com.github.msemitkin.financie.telegram.updatehandler.system.StartMessageHandler;
import com.github.msemitkin.financie.telegram.updatehandler.transaction.DeleteTransactionHandler;
import com.github.msemitkin.financie.telegram.updatehandler.transaction.GetDailyTransactionsHandler;
import com.github.msemitkin.financie.telegram.updatehandler.transaction.GetMonthlyCategoryTransactionsHandler;
import com.github.msemitkin.financie.telegram.updatehandler.transaction.GetTransactionActionsMenuHandler;
import com.github.msemitkin.financie.telegram.updatehandler.transaction.SaveTransactionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UpdateHandlerChainConfig {

    @Bean
    public UpdateHandlerChain updateHandlerChain(
        StartMessageHandler startMessageHandler,
        HelpHandler helpHandler,
        AuthorHandler authorHandler,
        GetDailyCategoriesHandler getDailyCategoriesHandler,
        GetDailyTransactionsHandler getDailyTransactionsHandler,
        GetMonthlyCategoriesHandler getMonthlyCategoriesHandler,
        GetMonthlyCategoryTransactionsHandler getMonthlyCategoryTransactionsHandler,
        GetThisMonthCategoriesHandler getThisMonthCategoriesHandler,
        GetTodayCategoriesHandler getTodayCategoriesHandler,
        GetTransactionActionsMenuHandler getTransactionActionsMenuHandler,
        DeleteTransactionHandler deleteTransactionHandler,
        MenuHandler menuHandler,
        MenuStateHandler menuStateHandler,
        ImportStateHandler importStateHandler,
        SaveTransactionHandler saveTransactionHandler,
        SettingsStateHandler settingsStateHandler,
        DefaultUpdateHandler defaultUpdateHandler
    ) {
        return UpdateHandlerChain.builder()
            .addHandler(startMessageHandler)
            .addHandler(helpHandler)
            .addHandler(authorHandler)
            .addHandler(getDailyCategoriesHandler)
            .addHandler(getDailyTransactionsHandler)
            .addHandler(getMonthlyCategoriesHandler)
            .addHandler(getMonthlyCategoryTransactionsHandler)
            .addHandler(getTransactionActionsMenuHandler)
            .addHandler(deleteTransactionHandler)
            .addHandler(menuHandler)
            .addHandler(menuStateHandler)
            .addHandler(importStateHandler)
            .addHandler(settingsStateHandler)
            .addHandler(getTodayCategoriesHandler)
            .addHandler(getThisMonthCategoriesHandler)
            .addHandler(saveTransactionHandler)
            .addHandler(defaultUpdateHandler)
            .build();
    }
}
