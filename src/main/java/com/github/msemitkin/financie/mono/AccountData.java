package com.github.msemitkin.financie.mono;

public class AccountData {
    private String account;
    private StatementItem statementItem;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public StatementItem getStatementItem() {
        return statementItem;
    }

    public void setStatementItem(StatementItem statementItem) {
        this.statementItem = statementItem;
    }

    @Override
    public String toString() {
        return "AccountData{" +
               "account='" + account + '\'' +
               ", statementItem=" + statementItem +
               '}';
    }
}
