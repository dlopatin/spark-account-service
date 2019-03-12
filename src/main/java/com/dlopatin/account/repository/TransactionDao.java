package com.dlopatin.account.repository;

import com.dlopatin.account.model.Transaction;

import java.util.List;

/**
 * Provides interface to work with transaction repository.
 */
public interface TransactionDao {

    /**
     * Stores transaction.
     *
     * @param transaction transaction to be stored
     * @return <code>true</code> if transaction was created, <code>false</code> otherwise
     */
    boolean insert(Transaction transaction);

    /**
     * List existing transactions by operation id.
     *
     * @param operationId unique operation id provided by client
     * @return list of stored transactions
     */
    List<Transaction> list(int operationId);

    /**
     * Checks whether transactions exist by given operation id.
     *
     * @param operationId unique operation id provided by client
     * @return <code>true</code> if any transaction exists, <code>false</code> otherwise
     */
    boolean hasTransactions(int operationId);
}
