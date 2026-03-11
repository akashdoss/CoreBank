package com.corebank.util;

import com.corebank.model.Account;
import com.corebank.model.Transaction;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple JSON Utility.
 * Demonstrates basic String manipulation and Streams for JSON serialization.
 */
public class JsonUtil {

    public static String toJson(Account account) {
        return String.format(
                "{\"accountNumber\": \"%s\", \"userId\": \"%s\", \"balance\": %s, \"status\": \"%s\", \"type\": \"%s\"}",
                account.getAccountNumber(), account.getUserId(), account.getBalance(), account.getStatus(),
                account.getType());
    }

    public static String toJson(Transaction tx) {
        return String.format(
                "{\"id\": \"%s\", \"source\": \"%s\", \"target\": \"%s\", \"amount\": %s, \"type\": \"%s\", \"status\": \"%s\", \"timestamp\": \"%s\"}",
                tx.getId(), tx.getSourceAccountNumber(), tx.getTargetAccountNumber(), tx.getAmount(), tx.getType(),
                tx.getStatus(), tx.getTimestamp());
    }

    public static String accountsToJson(List<Account> accounts) {
        return "[" + accounts.stream().map(JsonUtil::toJson).collect(Collectors.joining(",")) + "]";
    }

    public static String transactionsToJson(List<Transaction> txs) {
        return "[" + txs.stream().map(JsonUtil::toJson).collect(Collectors.joining(",")) + "]";
    }

    public static String getValue(String json, String key) {
        String pattern = "\"" + key + "\":\\s*\"?([^,\"}]+)\"?";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    public static String errorToJson(String message) {
        return String.format("{\"error\": \"%s\"}", message.replace("\"", "\\\""));
    }
}
