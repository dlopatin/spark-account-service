package com.dlopatin.account.service;

import com.dlopatin.account.model.Account;
import com.dlopatin.account.model.Currency;

import java.util.Optional;

/**
 * Performs operation with account.
 */
public interface AccountService {

    /**
     * Creates new account with given balance. It's a simple test application, so money come from air.
     * No need in any system account.
     *
     * @param currency currency of account
     * @param balance  initial balance.
     * @return created account
     * @throws IllegalArgumentException in case of incorrect arguments
     */
    Account create(Currency currency, long balance);

    /**
     * Retrieves account by it' id.
     *
     * @param id account id
     * @return account wrapped in optional
     */
    Optional<Account> get(int id);

    /**
     * @param from        account id to withdraw money
     * @param to          account id to put money
     * @param amount      amount in cents, pennies etc
     * @param operationId operation id to avoid transfer duplication from client
     * @throws TransferException if transfer can not be completed
     */
    void transfer(int from, int to, long amount, int operationId);
}
