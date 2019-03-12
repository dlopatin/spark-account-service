package com.dlopatin.account.service;

/**
 * Exception during transfer request procession
 */
public class TransferException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final TransferError transferError;

    public TransferException(TransferError transferError) {
        super(transferError.toString());
        this.transferError = transferError;
    }

    public TransferError getTransferError() {
        return transferError;
    }

    public enum TransferError {
        INSUFFICIENT_BALANCE,
        ACCOUNT_FROM_NOT_FOUND,
        ACCOUNT_TO_NOT_FOUND,
        DIFFERENT_ACCOUNT_CURRENCIES,
        TRANSFER_ALREADY_PROCESSED
    }
}
