package com.corebank.service;

import com.corebank.exception.AccountNotFoundException;
import com.corebank.model.Account;
import com.corebank.repository.Repository;

import java.util.List;

/**
 * Service for managing bank accounts.
 */
public class AccountService {
    private final Repository<Account, String> accountRepository;

    public AccountService(Repository<Account, String> accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void createAccount(Account account) {
        accountRepository.save(account);
    }

    public Account getAccount(String accountNumber) {
        return accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}
