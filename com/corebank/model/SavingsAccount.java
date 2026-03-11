package com.corebank.model;

import java.math.BigDecimal;

/**
 * Concrete Savings Account.
 * Demonstrates Inheritance.
 */
public class SavingsAccount extends Account {
    private static final long serialVersionUID = 1L;
    private final BigDecimal interestRate;

    public SavingsAccount(String accountNumber, String userId, BigDecimal initialBalance, BigDecimal interestRate) {
        super(accountNumber, userId, initialBalance, AccountType.SAVINGS);
        this.interestRate = interestRate;
    }

    public BigDecimal getInterestRate() { return interestRate; }

    @Override
    public String toString() {
        return "Savings" + super.toString() + " [Interest Rate=" + interestRate + "]";
    }
}
