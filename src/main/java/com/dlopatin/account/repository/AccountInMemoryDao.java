package com.dlopatin.account.repository;

import com.dlopatin.account.model.Account;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AccountInMemoryDao implements AccountDao {
    private final Map<Integer, Account> accountStorage;

    public AccountInMemoryDao() {
        accountStorage = new ConcurrentHashMap<>();
    }

    @Override
    public boolean create(Account account) {
        Account stored = accountStorage.putIfAbsent(account.getId(), account);
        return account.equals(stored);
    }

    @Override
    public void update(Account account) {
        // no action as account updates by link
    }

    @Override
    public Optional<Account> get(int id) {
        return Optional.ofNullable(accountStorage.get(id));
    }
}
