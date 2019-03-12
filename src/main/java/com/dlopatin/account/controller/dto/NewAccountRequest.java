package com.dlopatin.account.controller.dto;

import com.dlopatin.account.model.Currency;

public class NewAccountRequest {

    private Currency currency;
    // balance is stored in cents, pinnies, etc. To avoid work with BigDecimal
    private long balance;

    public NewAccountRequest() {
    }

    public Currency getCurrency() {
        return currency;
    }

    public long getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "NewAccountRequest{" +
                "currency=" + currency +
                ", balance=" + balance +
                '}';
    }
}
