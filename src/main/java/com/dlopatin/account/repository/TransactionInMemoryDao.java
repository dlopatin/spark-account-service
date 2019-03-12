package com.dlopatin.account.repository;

import com.dlopatin.account.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionInMemoryDao implements TransactionDao {

    /**
     * Stores transactions mapped by operation id
     */
    private final Map<Integer, Transaction> debitTransactionStorage;
    /**
     * Stores transactions mapped by operation id
     */
    private final Map<Integer, Transaction> creditTransactionStorage;

    public TransactionInMemoryDao() {
        debitTransactionStorage = new ConcurrentHashMap<>();
        creditTransactionStorage = new ConcurrentHashMap<>();
    }

    @Override
    public boolean insert(Transaction transaction) {
        Transaction stored;
        switch (transaction.getType()) {
            case CREDIT:
                stored = creditTransactionStorage.putIfAbsent(transaction.getOperationId(), transaction);
                break;
            case DEBIT:
                stored = debitTransactionStorage.putIfAbsent(transaction.getOperationId(), transaction);
                break;
            default:
                throw new UnsupportedOperationException("Transaction type not supported: " + transaction.getType());
        }
        return transaction.equals(stored);
    }

    @Override
    public List<Transaction> list(int operationId) {
        List<Transaction> transactions = new ArrayList<>();
        Optional.ofNullable(debitTransactionStorage.get(operationId)).ifPresent(transactions::add);
        Optional.ofNullable(creditTransactionStorage.get(operationId)).ifPresent(transactions::add);
        return transactions;
    }

    @Override
    public boolean hasTransactions(int operationId) {
        return !list(operationId).isEmpty();
    }
}
