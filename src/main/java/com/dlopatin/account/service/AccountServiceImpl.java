package com.dlopatin.account.service;

import com.dlopatin.account.model.Account;
import com.dlopatin.account.model.Currency;
import com.dlopatin.account.model.Transaction;
import com.dlopatin.account.model.TransactionType;
import com.dlopatin.account.repository.AccountDao;
import com.dlopatin.account.repository.TransactionDao;
import com.dlopatin.account.service.TransferException.TransferError;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountServiceImpl implements AccountService {

    private final AtomicInteger accountIdGenerator = new AtomicInteger();

    private final AccountDao accountDao;
    private final TransactionDao transactionDao;

    public AccountServiceImpl(AccountDao accountDao, TransactionDao transactionDao) {
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
    }

    @Override
    public Account create(Currency currency, long balance) {
        checkNotNull(currency, "currency");
        checkNotNegative(balance);
        // TODO: add operation id to avoid creation duplication
        // TODO: add transaction recording
        Account newAccount = new Account(accountIdGenerator.incrementAndGet(), currency, balance);
        accountDao.create(newAccount);
        return newAccount;
    }

    @Override
    public Optional<Account> get(int id) {
        return accountDao.get(id);
    }

    @Override
    public void transfer(int fromId, int toId, long amount, int operationId) {
        checkNotNegative(amount);
        Account from = get(fromId).orElseThrow(() -> new TransferException(TransferError.ACCOUNT_FROM_NOT_FOUND));
        Account to = get(toId).orElseThrow(() -> new TransferException(TransferError.ACCOUNT_TO_NOT_FOUND));
        if (from.getCurrency() != to.getCurrency()) {
            throw new TransferException(TransferError.DIFFERENT_ACCOUNT_CURRENCIES);
        }
        Account first = from.getId() < to.getId() ? from : to;
        Account second = from.getId() < to.getId() ? to : from;
        synchronized (first.getLock()) {
            synchronized (second.getLock()) {
                if (amount > from.getBalance()) {
                    throw new TransferException(TransferError.INSUFFICIENT_BALANCE);
                }
                if (transactionDao.hasTransactions(operationId)) {
                    throw new TransferException(TransferError.TRANSFER_ALREADY_PROCESSED);
                }
                from.setBalance(from.getBalance() - amount);
                from.incrementVersion();
                to.setBalance(to.getBalance() + amount);
                to.incrementVersion();
                transactionDao.insert(new Transaction(operationId, fromId, TransactionType.DEBIT, amount));
                transactionDao.insert(new Transaction(operationId, toId, TransactionType.CREDIT, amount));
                accountDao.update(first);
                accountDao.update(second);
            }
        }
    }

    private void checkNotNull(Object value, String fieldName) {
        Optional.ofNullable(value).orElseThrow(() -> new IllegalArgumentException(
                String.format("%s can not be null", fieldName)));
    }

    private void checkNotNegative(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Balance is negative");
        }
    }
}
