package com.corebank;

import com.corebank.model.*;
import com.corebank.repository.FileRepository;
import com.corebank.repository.Repository;
import com.corebank.service.AccountService;
import com.corebank.service.TransactionService;
import com.corebank.util.AnalyticsEngine;
import com.corebank.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.List;

public class CoreBankServer {
    private static AccountService accountService;
    private static TransactionService transactionService;
    private static AnalyticsEngine analyticsEngine;

    public static void main(String[] args) throws IOException {
        // Initialize Core Components
        Repository<Account, String> accountRepo = new FileRepository<>("data/accounts.dat", Account::getAccountNumber);
        Repository<Transaction, String> transactionRepo = new FileRepository<>("data/transactions.dat",
                Transaction::getId);

        accountService = new AccountService(accountRepo);
        transactionService = new TransactionService(accountService, transactionRepo);
        analyticsEngine = new AnalyticsEngine(transactionRepo);

        // Bootstrap sample data if empty
        if (accountService.getAllAccounts().isEmpty()) {
            accountService.createAccount(
                    new SavingsAccount("ACC001", "USER1", new BigDecimal("5000"), new BigDecimal("0.05")));
            accountService.createAccount(
                    new SavingsAccount("ACC002", "USER2", new BigDecimal("3000"), new BigDecimal("0.05")));
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // CORS and Options support
        server.createContext("/api/accounts", new AccountHandler());
        server.createContext("/api/transactions", new TransactionHandler());
        server.createContext("/api/analytics", new AnalyticsHandler());

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        System.out.println("CoreBank Pro Server started on port 8080");
        server.start();
    }

    static class AccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String response = JsonUtil.accountsToJson(accountService.getAllAccounts());
            sendResponse(exchange, response, 200);
        }
    }

    static class TransactionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, JsonUtil.transactionsToJson(
                        transactionService.deposit("ACC001", BigDecimal.ZERO).getId() != null ? List.of() : List.of()),
                        200); // Dummy for now
            } else if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String body = new String(exchange.getRequestBody().readAllBytes());
                    String type = JsonUtil.getValue(body, "type");
                    String source = JsonUtil.getValue(body, "source");
                    String target = JsonUtil.getValue(body, "target");
                    String amountStr = JsonUtil.getValue(body, "amount");
                    BigDecimal amount = new BigDecimal(amountStr);

                    Transaction tx = null;
                    if ("DEPOSIT".equals(type)) {
                        tx = transactionService.deposit(source, amount);
                    } else if ("WITHDRAWAL".equals(type)) {
                        tx = transactionService.withdraw(source, amount);
                    } else if ("TRANSFER".equals(type)) {
                        tx = transactionService.transfer(source, target, amount);
                    }

                    if (tx != null) {
                        sendResponse(exchange, JsonUtil.toJson(tx), 200);
                    } else {
                        sendResponse(exchange, JsonUtil.errorToJson("Invalid transaction type"), 400);
                    }
                } catch (Exception e) {
                    sendResponse(exchange, JsonUtil.errorToJson(e.getMessage()), 500);
                }
            }
        }
    }

    static class AnalyticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCors(exchange);
            BigDecimal totalVolume = analyticsEngine.getTotalTransactionVolume();
            int highValueCount = analyticsEngine.getHighValueTransactions(new BigDecimal("1000")).size();
            String response = String.format("{\"totalVolume\": %s, \"highValueCount\": %d, \"status\": \"OPTIMAL\"}",
                    totalVolume, highValueCount);
            sendResponse(exchange, response, 200);
        }
    }

    private static void setCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
