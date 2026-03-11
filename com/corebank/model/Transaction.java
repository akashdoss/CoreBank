package com.corebank.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a financial transaction.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String id;
    private final String sourceAccountNumber;
    private final String targetAccountNumber; // Optional
    private final BigDecimal amount;
    private final TransactionType type;
    private final LocalDateTime timestamp;
    private TransactionStatus status;

    public Transaction(String id, String sourceAccountNumber, String targetAccountNumber, BigDecimal amount, TransactionType type) {
        this.id = id;
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.amount = amount;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
    }

    public String getId() { return id; }
    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public String getTargetAccountNumber() { return targetAccountNumber; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Transaction{id='" + id + "', type=" + type + ", amount=" + amount + ", status=" + status + ", time=" + timestamp + "}";
    }
}
