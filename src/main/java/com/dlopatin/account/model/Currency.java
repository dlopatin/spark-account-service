package com.dlopatin.account.model;

/**
 * Account currencies
 */
public enum Currency {

    RUB("RUB"),
    USD("USD"),
    EUR("EUR"),
    GBP("GBP");

    private final java.util.Currency currency;

    Currency(String currency) {
        this.currency = java.util.Currency.getInstance(currency);
    }

    public java.util.Currency getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return getCurrency().toString();
    }
}
