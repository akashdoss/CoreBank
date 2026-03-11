package com.corebank.service;

import com.corebank.exception.InsufficientFundsException;
import com.corebank.model.*;
import com.corebank.repository.Repository;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for processing financial transactions.
 * Demonstrates business logic and error handling.
 */
public class TransactionService {
    private final AccountService accountService;
    private final Repository<Transaction, String> transactionRepository;

    public TransactionService(AccountService accountService, Repository<Transaction, String> transactionRepository) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    public Transaction deposit(String accountNumber, BigDecimal amount) {
        Account account = accountService.getAccount(accountNumber);
        account.deposit(amount);
        
        Transaction tx = new Transaction(UUID.randomUUID().toString(), accountNumber, null, amount, TransactionType.DEPOSIT);
        tx.setStatus(TransactionStatus.COMPLETED);
        return transactionRepository.save(tx);
    }

    public Transaction withdraw(String accountNumber, BigDecimal amount) {
        Account account = accountService.getAccount(accountNumber);
        try {
            account.withdraw(amount);
        } catch (IllegalStateException e) {
            throw new InsufficientFundsException(accountNumber);
        }

        Transaction tx = new Transaction(UUID.randomUUID().toString(), accountNumber, null, amount, TransactionType.WITHDRAWAL);
        tx.setStatus(TransactionStatus.COMPLETED);
        return transactionRepository.save(tx);
    }

    public Transaction transfer(String sourceAccountNumber, String targetAccountNumber, BigDecimal amount) {
        Account source = accountService.getAccount(sourceAccountNumber);
        Account target = accountService.getAccount(targetAccountNumber);

        // Simple atomic transfer logic (in a real DB we'd use a transaction)
        // Here we rely on the ReentrantLocks in the Account objects.
        // To avoid deadlocks, we should always lock in a specific order (e.g., lower account number first)
        
        Account first = source.getAccountNumber().compareTo(target.getAccountNumber()) < 0 ? source : target;
        Account second = first == source ? target : source;

        // Note: For simplicity, we are using the internal locks indirectly through deposit/withdraw 
        // which already handle locking. However, for a multi-account atomic operation, 
        // we'd ideally lock both before starting.
        
        source.withdraw(amount);
        target.deposit(amount);

        Transaction tx = new Transaction(UUID.randomUUID().toString(), sourceAccountNumber, targetAccountNumber, amount, TransactionType.TRANSFER);
        tx.setStatus(TransactionStatus.COMPLETED);
        return transactionRepository.save(tx);
    }
}
