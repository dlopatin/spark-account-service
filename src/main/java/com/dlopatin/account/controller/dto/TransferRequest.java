package com.dlopatin.account.controller.dto;

public class TransferRequest {

    private int accountFrom;
    private int accountTo;
    // amount is stored in cents, pinnies, etc. To avoid work with BigDecimal
    private long amount;
    private int operationId;

    public TransferRequest() {
    }

    public int getAccountFrom() {
        return accountFrom;
    }

    public int getAccountTo() {
        return accountTo;
    }

    public long getAmount() {
        return amount;
    }

    public int getOperationId() {
        return operationId;
    }

    @Override
    public String toString() {
        return "TransferRequest{" +
                "accountFrom=" + accountFrom +
                ", accountTo=" + accountTo +
                ", amount=" + amount +
                '}';
    }
}
