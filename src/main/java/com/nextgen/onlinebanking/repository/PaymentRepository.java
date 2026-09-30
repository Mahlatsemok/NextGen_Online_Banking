package com.nextgen.onlinebanking.repository;

import com.nextgen.onlinebanking.model.Payment;
import com.nextgen.onlinebanking.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentReference(
            String paymentReference);

    Optional<Payment> findByIdempotencyKey(
            String idempotencyKey);

    List<Payment> findBySourceAccountId(
            Long sourceAccountId);

    List<Payment> findByDestinationAccountId(
            Long destinationAccountId);

    List<Payment> findByStatus(
            PaymentStatus status);

    List<Payment> findByStatusAndScheduledAtLessThanEqual(
            PaymentStatus status,
            LocalDateTime dateTime);

    boolean existsByPaymentReference(
            String paymentReference);

    boolean existsByIdempotencyKey(
            String idempotencyKey);
}
