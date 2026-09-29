package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.Transaction;
import com.nextgen.onlinebanking.model.TransactionType;
import com.nextgen.onlinebanking.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction recordTransaction(
            TransactionType type,
            BigDecimal amount,
            BankAccount sourceAccount,
            BankAccount destinationAccount) {

        return recordTransaction(
                type,
                amount,
                sourceAccount,
                destinationAccount,
                null);
    }

    public Transaction recordTransaction(
            TransactionType type,
            BigDecimal amount,
            BankAccount sourceAccount,
            BankAccount destinationAccount,
            String idempotencyKey) {

        if (type == null) {
            throw new IllegalArgumentException(
                    "Transaction type is required");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero");
        }

        Transaction transaction = new Transaction();

        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setIdempotencyKey(idempotencyKey);

        return transactionRepository.save(transaction);
    }

    public Optional<Transaction> findByIdempotencyKey(
            String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }

        return transactionRepository
                .findByIdempotencyKey(idempotencyKey);
    }

    public List<Transaction> getTransactionsForAccount(
            BankAccount account) {

        if (account == null) {
            throw new IllegalArgumentException(
                    "Account is required");
        }

        return transactionRepository
                .findBySourceAccountIdOrDestinationAccountId(
                        account.getId(),
                        account.getId());
    }
}
