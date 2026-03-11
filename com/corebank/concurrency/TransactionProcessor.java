package com.corebank.concurrency;

import com.corebank.model.Transaction;
import com.corebank.model.TransactionStatus;
import com.corebank.service.TransactionService;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Multi-threaded Transaction Processor.
 * Demonstrates Threading, Runnable, and Synchronization patterns.
 */
public class TransactionProcessor {
    private final TransactionService transactionService;
    private final BlockingQueue<TransactionRequest> requestQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor;
    private volatile boolean running = true;

    public TransactionProcessor(TransactionService transactionService, int threadCount) {
        this.transactionService = transactionService;
        this.executor = Executors.newFixedThreadPool(threadCount);
        startWorker();
    }

    private void startWorker() {
        Thread workerThread = new Thread(() -> {
            while (running) {
                try {
                    TransactionRequest request = requestQueue.take();
                    executor.submit(() -> processRequest(request));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        workerThread.setName("Transaction-Worker-Main");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public void submitTransaction(TransactionRequest request) {
        requestQueue.offer(request);
    }

    private void processRequest(TransactionRequest request) {
        try {
            switch (request.getType()) {
                case DEPOSIT:
                    transactionService.deposit(request.getSourceAccount(), request.getAmount());
                    break;
                case WITHDRAWAL:
                    transactionService.withdraw(request.getSourceAccount(), request.getAmount());
                    break;
                case TRANSFER:
                    transactionService.transfer(request.getSourceAccount(), request.getTargetAccount(), request.getAmount());
                    break;
            }
            System.out.println("[Processor] Processed: " + request);
        } catch (Exception e) {
            System.err.println("[Processor] Failed to process: " + request + " - Error: " + e.getMessage());
        }
    }

    public void shutdown() {
        running = false;
        executor.shutdown();
    }

    public static class TransactionRequest {
        private final String sourceAccount;
        private final String targetAccount;
        private final java.math.BigDecimal amount;
        private final com.corebank.model.TransactionType type;

        public TransactionRequest(String sourceAccount, String targetAccount, java.math.BigDecimal amount, com.corebank.model.TransactionType type) {
            this.sourceAccount = sourceAccount;
            this.targetAccount = targetAccount;
            this.amount = amount;
            this.type = type;
        }

        public String getSourceAccount() { return sourceAccount; }
        public String getTargetAccount() { return targetAccount; }
        public java.math.BigDecimal getAmount() { return amount; }
        public com.corebank.model.TransactionType getType() { return type; }

        @Override
        public String toString() {
            return "TxRequest{" + type + ", amount=" + amount + "}";
        }
    }
}
