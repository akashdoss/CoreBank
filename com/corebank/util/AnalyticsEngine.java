package com.corebank.util;

import com.corebank.model.Transaction;
import com.corebank.repository.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analytics Engine for banking reports.
 * Demonstrates Java 8 Streams, Lambdas, and Functional Programming.
 */
public class AnalyticsEngine {
    private final Repository<Transaction, String> transactionRepository;

    public AnalyticsEngine(Repository<Transaction, String> transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public BigDecimal getTotalTransactionVolume() {
        return transactionRepository.findAll().stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Transaction> getHighValueTransactions(BigDecimal threshold) {
        return transactionRepository.findAll().stream()
                .filter(tx -> tx.getAmount().compareTo(threshold) > 0)
                .collect(Collectors.toList());
    }

    public Map<String, BigDecimal> getVolumeBySourceAccount() {
        return transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Transaction::getSourceAccountNumber,
                        Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }

    public void printReport() {
        System.out.println("========== COREBANK ANALYTICS REPORT ==========");
        System.out.println("Total Volume: $" + getTotalTransactionVolume());
        System.out.println("High Value Count (> 1000): " + getHighValueTransactions(new BigDecimal("1000")).size());
        System.out.println("===============================================");
    }
}
