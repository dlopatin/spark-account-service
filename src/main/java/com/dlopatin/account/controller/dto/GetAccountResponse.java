package com.dlopatin.account.controller.dto;

import com.dlopatin.account.model.Account;
import com.dlopatin.account.model.Currency;

public class GetAccountResponse {

    private final int id;
    private final Currency currency;
    // balance is stored in cents, pinnies, etc. To avoid work with BigDecimal
    private final long balance;

    public static GetAccountResponse from(Account account) {
        return new GetAccountResponse(account.getId(), account.getCurrency(), account.getBalance());
    }

    private GetAccountResponse(int id, Currency currency, long balance) {
        this.id = id;
        this.currency = currency;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public Currency getCurrency() {
        return currency;
    }

    public long getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "GetAccountResponse{" +
                "id=" + id +
                ", currency=" + currency +
                ", balance=" + balance +
                '}';
    }
}
