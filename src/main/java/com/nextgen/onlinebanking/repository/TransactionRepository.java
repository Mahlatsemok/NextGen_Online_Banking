package com.nextgen.onlinebanking.repository;

import com.nextgen.onlinebanking.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySourceAccountId(Long accountId);

    List<Transaction> findByDestinationAccountId(Long accountId);

    List<Transaction> findBySourceAccountIdOrDestinationAccountId(
            Long sourceAccountId,
            Long destinationAccountId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
