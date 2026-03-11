package com.corebank.exception;

public class InsufficientFundsException extends AppException {
    public InsufficientFundsException(String accountNumber) {
        super("Insufficient funds in account: " + accountNumber);
    }
}
