package com.github.msemitkin.financie.mono;

public class StatementItem {
    private String id;
    private long time;
    private String description;
    private int mcc;
    private boolean hold;
    private long amount;
    private long operationAmount;
    private int currencyCode;
    private long commissionRate;
    private long cashbackAmount;
    private long balance;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMcc() {
        return mcc;
    }

    public void setMcc(int mcc) {
        this.mcc = mcc;
    }

    public boolean isHold() {
        return hold;
    }

    public void setHold(boolean hold) {
        this.hold = hold;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getOperationAmount() {
        return operationAmount;
    }

    public void setOperationAmount(long operationAmount) {
        this.operationAmount = operationAmount;
    }

    public int getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(int currencyCode) {
        this.currencyCode = currencyCode;
    }

    public long getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(long commissionRate) {
        this.commissionRate = commissionRate;
    }

    public long getCashbackAmount() {
        return cashbackAmount;
    }

    public void setCashbackAmount(long cashbackAmount) {
        this.cashbackAmount = cashbackAmount;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "StatementItem{" +
               "id='" + id + '\'' +
               ", time=" + time +
               ", description='" + description + '\'' +
               ", mcc=" + mcc +
               ", hold=" + hold +
               ", amount=" + amount +
               ", operationAmount=" + operationAmount +
               ", currencyCode=" + currencyCode +
               ", commissionRate=" + commissionRate +
               ", cashbackAmount=" + cashbackAmount +
               ", balance=" + balance +
               '}';
    }
}
