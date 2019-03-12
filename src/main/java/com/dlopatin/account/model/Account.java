package com.dlopatin.account.model;

import java.util.Objects;

/**
 * Account entity
 */
public class Account {
    private final int id;
    private int version;
    private final Currency currency;
    // balance is stored in cents, pinnies, etc. To avoid work with BigDecimal
    private long balance;

    private final Object lock = new Object();

    public Account(int id, Currency currency, long balance) {
        this.id = id;
        this.currency = currency;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public Currency getCurrency() {
        return currency;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public int incrementVersion() {
        return ++version;
    }

    public Object getLock() {
        return lock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Account account = (Account) o;
        return id == account.id &&
                version == account.version &&
                balance == account.balance &&
                currency == account.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, currency, balance);
    }
}
