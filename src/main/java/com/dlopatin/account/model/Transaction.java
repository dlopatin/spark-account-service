package com.dlopatin.account.model;

/**
 * Represents transaction entity
 */
public class Transaction {

    private final int operationId;
    private final int accountId;
    private final TransactionType type;
    private final long amount;

    public Transaction(int operationId, int accountId, TransactionType type, long amount) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.operationId = operationId;
    }

    public int getAccountId() {
        return accountId;
    }

    public TransactionType getType() {
        return type;
    }

    public long getAmount() {
        return amount;
    }

    public int getOperationId() {
        return operationId;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "accountId=" + accountId +
                ", type=" + type +
                ", amount=" + amount +
                ", operationId=" + operationId +
                '}';
    }
}
