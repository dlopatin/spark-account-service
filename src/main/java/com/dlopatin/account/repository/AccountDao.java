package com.dlopatin.account.repository;

import com.dlopatin.account.model.Account;

import java.util.Optional;

/**
 * Provides interface to work with account repository.
 */
public interface AccountDao {

    /**
     * Stores account if it's not persisted.
     *
     * @param account account to be stored
     * @return <code>true</code> if account was created, <code>false</code> otherwise
     */
    boolean create(Account account);

    /**
     * Updates existing account
     *
     * @param account account to be updated
     */
    void update(Account account);

    /**
     * Retrieves account by its id.
     *
     * @param id account id
     * @return account wrapped in optional class
     */
    Optional<Account> get(int id);
}
