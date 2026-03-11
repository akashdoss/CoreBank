package com.corebank.exception;

public class AccountNotFoundException extends AppException {
    public AccountNotFoundException(String accountNumber) {
        super("Account not found: " + accountNumber);
    }
}
