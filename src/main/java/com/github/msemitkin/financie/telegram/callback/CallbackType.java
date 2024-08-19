package com.github.msemitkin.financie.telegram.callback;

public enum CallbackType {
    GET_CATEGORIES_FOR_DAY,
    GET_CATEGORIES_FOR_MONTH,
    GET_CATEGORY_TRANSACTIONS_FOR_DAY,
    GET_CATEGORY_TRANSACTIONS_FOR_MONTH,
    GET_TRANSACTION_ACTIONS,
    DELETE_TRANSACTION,
    ADD_TRANSACTION,
    GET_MONTHLY_REPORT,
}
