package com.corebank.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base Account class.
 * Demonstrates Abstraction, Encapsulation, and Thread-Safety using Locks.
 */
public abstract class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String accountNumber;
    private final String userId;
    private BigDecimal balance;
    private AccountStatus status;
    private final AccountType type;
    
    // Transient so it's not serialized. We re-init on de-serialization if needed, 
    // but better to manage locking at service level or use volatile/synchronized.
    // Putting it here for direct thread-safe balance updates.
    private transient ReentrantLock lock = new ReentrantLock();

    public Account(String accountNumber, String userId, BigDecimal initialBalance, AccountType type) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.balance = initialBalance;
        this.status = AccountStatus.ACTIVE;
        this.type = type;
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        lock = new ReentrantLock();
    }

    public String getAccountNumber() { return accountNumber; }
    public String getUserId() { return userId; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public AccountType getType() { return type; }

    public BigDecimal getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }

    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        lock.lock();
        try {
            this.balance = this.balance.add(amount);
        } finally {
            lock.unlock();
        }
    }

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        lock.lock();
        try {
            if (this.balance.compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient funds");
            }
            this.balance = this.balance.subtract(amount);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "Account{number='" + accountNumber + "', balance=" + balance + ", status=" + status + ", type=" + type + "}";
    }
}
