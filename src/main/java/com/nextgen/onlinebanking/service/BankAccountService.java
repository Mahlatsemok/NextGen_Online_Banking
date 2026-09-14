package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.AccountStatus;
import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.repository.BankAccountRepository;
import com.nextgen.onlinebanking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;

@Service
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    public BankAccountService(
            BankAccountRepository bankAccountRepository,
            UserRepository userRepository) {

        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    public BankAccount createAccount(Long userId, AccountType accountType) {

        if (accountType == null) {
            throw new IllegalArgumentException("Account type is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String accountNumber = generateUniqueAccountNumber();

        BankAccount account = new BankAccount();

        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setAccountType(accountType);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);

        return bankAccountRepository.save(account);
    }
    
    public List<BankAccount> getAccountsForUser(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return bankAccountRepository.findByUserId(userId);
    }

    public BankAccount getAccountByAccountNumber(String accountNumber) {

        return bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public BankAccount deposit(String accountNumber, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        BankAccount account = bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.setBalance(account.getBalance().add(amount));

        return bankAccountRepository.save(account);
    }


    public BankAccount withdraw(String accountNumber, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }

        BankAccount account = bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));

        return bankAccountRepository.save(account);
    }

    public AccountStatus getAccountStatus(String accountNumber) {

        BankAccount account = bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        return account.getStatus();
    }

    public BankAccount updateAccountStatus(
        String accountNumber,
            AccountStatus status) {

        if (status == null) {
            throw new IllegalArgumentException("Account status is required");
        }

        BankAccount account = bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.setStatus(status);

        return bankAccountRepository.save(account);
    }

    public BankAccount freezeAccount(String accountNumber) {

        BankAccount account = bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Closed account cannot be frozen");
        }

        account.setStatus(AccountStatus.FROZEN);

        return bankAccountRepository.save(account);
    }

    public BankAccount closeAccount(String accountNumber) {

        BankAccount account = bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Account is already closed");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(
                    "Account balance must be zero before closing");
        }

        account.setStatus(AccountStatus.CLOSED);

        return bankAccountRepository.save(account);
    }


    private String generateUniqueAccountNumber() {

        String accountNumber;

        do {
            accountNumber = generateAccountNumber();
        } while (bankAccountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private String generateAccountNumber() {

        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);
    }

    public BankAccount getAccountForUser(
        Long userId,
            String accountNumber) {

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BankAccount account = bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Account does not belong to user");
        }

        return account;
    }

}
