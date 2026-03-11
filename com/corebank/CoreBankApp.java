package com.corebank;

import com.corebank.concurrency.TransactionProcessor;
import com.corebank.model.*;
import com.corebank.repository.FileRepository;
import com.corebank.repository.Repository;
import com.corebank.service.AccountService;
import com.corebank.service.TransactionService;
import com.corebank.util.AnalyticsEngine;

import java.math.BigDecimal;
import java.util.Scanner;

public class CoreBankApp {
    private static final String ACCOUNTS_FILE = "data/accounts.dat";
    private static final String TRANSACTIONS_FILE = "data/transactions.dat";

    public static void main(String[] args) {
        new java.io.File("data").mkdirs();

        // Initialize Persistence
        Repository<Account, String> accountRepo = new FileRepository<>(ACCOUNTS_FILE, Account::getAccountNumber);
        Repository<Transaction, String> transactionRepo = new FileRepository<>(TRANSACTIONS_FILE, Transaction::getId);

        // Initialize Services
        AccountService accountService = new AccountService(accountRepo);
        TransactionService transactionService = new TransactionService(accountService, transactionRepo);
        
        // Initialize Concurrency Engine
        TransactionProcessor processor = new TransactionProcessor(transactionService, 4);

        // Initialize Analytics
        AnalyticsEngine analytics = new AnalyticsEngine(transactionRepo);

        System.out.println("Welcome to CoreBank - Industrial Strength Java System");
        
        // Bootstrap sample data if empty
        if (accountService.getAllAccounts().isEmpty()) {
            System.out.println("[Init] Bootstrapping sample accounts...");
            accountService.createAccount(new SavingsAccount("ACC001", "USER1", new BigDecimal("5000"), new BigDecimal("0.05")));
            accountService.createAccount(new SavingsAccount("ACC002", "USER2", new BigDecimal("3000"), new BigDecimal("0.05")));
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. View Accounts");
            System.out.println("2. Deposit (Instant)");
            System.out.println("3. Withdraw (Instant)");
            System.out.println("4. Queue Transfer (Async)");
            System.out.println("5. Run Analytics Report");
            System.out.println("6. Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        accountService.getAllAccounts().forEach(System.out::println);
                        break;
                    case "2":
                        System.out.print("Account Number: ");
                        String acc = scanner.nextLine();
                        System.out.print("Amount: ");
                        BigDecimal depAmt = new BigDecimal(scanner.nextLine());
                        transactionService.deposit(acc, depAmt);
                        System.out.println("Deposit successful!");
                        break;
                    case "3":
                        System.out.print("Account Number: ");
                        String wAcc = scanner.nextLine();
                        System.out.print("Amount: ");
                        BigDecimal wAmt = new BigDecimal(scanner.nextLine());
                        transactionService.withdraw(wAcc, wAmt);
                        System.out.println("Withdrawal successful!");
                        break;
                    case "4":
                        System.out.print("Source Account: ");
                        String src = scanner.nextLine();
                        System.out.print("Target Account: ");
                        String dst = scanner.nextLine();
                        System.out.print("Amount: ");
                        BigDecimal tAmt = new BigDecimal(scanner.nextLine());
                        processor.submitTransaction(new TransactionProcessor.TransactionRequest(src, dst, tAmt, TransactionType.TRANSFER));
                        System.out.println("Transfer submitted to background processor.");
                        break;
                    case "5":
                        analytics.printReport();
                        break;
                    case "6":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        processor.shutdown();
        System.out.println("Thank you for using CoreBank. State saved.");
    }
}
