package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.Payment;
import com.nextgen.onlinebanking.model.PaymentStatus;
import com.nextgen.onlinebanking.model.PaymentType;
import com.nextgen.onlinebanking.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BankAccountService bankAccountService;

    /*
     * Prevents concurrent requests using the same payment idempotency key
     * from creating duplicate Payment records.
     */
    private final ConcurrentMap<String, Object> idempotencyLocks =
            new ConcurrentHashMap<>();

    public PaymentService(
            PaymentRepository paymentRepository,
            BankAccountService bankAccountService) {

        this.paymentRepository = paymentRepository;
        this.bankAccountService = bankAccountService;
    }

    @Transactional
    public Payment createPayment(
            Long userId,
            String sourceAccountNumber,
            String destinationAccountNumber,
            BigDecimal amount,
            PaymentType type,
            String description,
            String idempotencyKey) {

        validatePayment(
                sourceAccountNumber,
                destinationAccountNumber,
                amount,
                type);

        if (type != PaymentType.STANDARD) {
            throw new IllegalArgumentException(
                    "Only standard payments are supported");
        }

        String key = normalizeIdempotencyKey(idempotencyKey);

        if (key == null) {
            return processStandardPayment(
                    userId,
                    sourceAccountNumber,
                    destinationAccountNumber,
                    amount,
                    type,
                    description,
                    null);
        }

        Object lock = idempotencyLocks.computeIfAbsent(
                key,
                ignored -> new Object());

        synchronized (lock) {

            Payment existingPayment = paymentRepository.findByIdempotencyKey(key)
                    .orElse(null);

            if (existingPayment != null) {

                validateIdempotentRetry(
                        existingPayment,
                        sourceAccountNumber,
                        destinationAccountNumber,
                        amount,
                        type);

                return existingPayment;
            }

            return processStandardPayment(
                    userId,
                    sourceAccountNumber,
                    destinationAccountNumber,
                    amount,
                    type,
                    description,
                    key);
        }
    }

    @Transactional
    public Payment createQrPayment(
            Long userId,
            String sourceAccountNumber,
            String destinationAccountNumber,
            BigDecimal amount,
            String description,
            String idempotencyKey) {

        validatePayment(
                sourceAccountNumber,
                destinationAccountNumber,
                amount,
                PaymentType.QR);

        String key = normalizeIdempotencyKey(idempotencyKey);

        if (key == null) {

            return processStandardPayment(
                    userId,
                    sourceAccountNumber,
                    destinationAccountNumber,
                    amount,
                    PaymentType.QR,
                    description,
                    null);
        }

        Object lock = idempotencyLocks.computeIfAbsent(
                key,
                ignored -> new Object());

        synchronized (lock) {

            Payment existingPayment = paymentRepository
                    .findByIdempotencyKey(key)
                    .orElse(null);

            if (existingPayment != null) {

                validateIdempotentRetry(
                        existingPayment,
                        sourceAccountNumber,
                        destinationAccountNumber,
                        amount,
                        PaymentType.QR);

                return existingPayment;
            }

            return processStandardPayment(
                    userId,
                    sourceAccountNumber,
                    destinationAccountNumber,
                    amount,
                    PaymentType.QR,
                    description,
                    key);

        }
    }
    
    @Transactional
    public Payment schedulePayment(
        Long userId,
        String sourceAccountNumber,
        String destinationAccountNumber,
        BigDecimal amount,
        java.time.LocalDateTime scheduledAt,
        String description,
            String idempotencyKey) {

        validatePayment(
                sourceAccountNumber,
                destinationAccountNumber,
                amount,
                PaymentType.SCHEDULED);

        if (scheduledAt == null) {
            throw new IllegalArgumentException(
                    "Scheduled date and time is required");
        }

        if (!scheduledAt.isAfter(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Scheduled date and time must be in the future");
        }

        String key = normalizeIdempotencyKey(idempotencyKey);

        if (key == null) {
            return createScheduledPayment(
                    userId,
                    sourceAccountNumber,
                    destinationAccountNumber,
                    amount,
                    scheduledAt,
                    description,
                    null);
        }

        Object lock = idempotencyLocks.computeIfAbsent(
                key,
                ignored -> new Object());

        synchronized (lock) {

            Payment existingPayment = paymentRepository.findByIdempotencyKey(key)
                    .orElse(null);

            if (existingPayment != null) {

                validateScheduledIdempotentRetry(
                        existingPayment,
                        sourceAccountNumber,
                        destinationAccountNumber,
                        amount,
                        scheduledAt);

                return existingPayment;
            }

            return createScheduledPayment(
                    userId,
                    sourceAccountNumber,
                    destinationAccountNumber,
                    amount,
                    scheduledAt,
                    description,
                    key);
        }
    }

    private Payment createScheduledPayment(
        Long userId,
        String sourceAccountNumber,
        String destinationAccountNumber,
        BigDecimal amount,
        java.time.LocalDateTime scheduledAt,
        String description,
            String idempotencyKey) {

        BankAccount sourceAccount = bankAccountService.getAccountForUser(
                userId,
                sourceAccountNumber);

        BankAccount destinationAccount = bankAccountService.getAccountByAccountNumber(
                destinationAccountNumber);

        Payment payment = new Payment();

        payment.setAmount(amount);
        payment.setType(PaymentType.SCHEDULED);
        payment.setSourceAccount(sourceAccount);
        payment.setDestinationAccount(destinationAccount);
        payment.setDescription(description);
        payment.setIdempotencyKey(idempotencyKey);
        payment.setScheduledAt(scheduledAt);
        payment.setStatus(PaymentStatus.SCHEDULED);

        return paymentRepository.save(payment);
    }

    private void validateScheduledIdempotentRetry(
        Payment existingPayment,
        String sourceAccountNumber,
        String destinationAccountNumber,
        BigDecimal amount,
            java.time.LocalDateTime scheduledAt) {

        boolean sameRequest = existingPayment.getAmount().compareTo(amount) == 0
                && existingPayment.getType() == PaymentType.SCHEDULED
                && existingPayment.getSourceAccount()
                        .getAccountNumber()
                        .equals(sourceAccountNumber)
                && existingPayment.getDestinationAccount()
                        .getAccountNumber()
                        .equals(destinationAccountNumber)
                && existingPayment.getScheduledAt()
                        .equals(scheduledAt);

        if (!sameRequest) {
            throw new IllegalArgumentException(
                    "Idempotency key has already been used for a different scheduled payment");
        }
    }


    private Payment processStandardPayment(
            Long userId,
            String sourceAccountNumber,
            String destinationAccountNumber,
            BigDecimal amount,
            PaymentType type,
            String description,
            String idempotencyKey) {

        /*
         * Delegate the actual money movement to BankAccountService.
         * This keeps transfer rules in one place.
         */
        bankAccountService.transfer(
                userId,
                sourceAccountNumber,
                destinationAccountNumber,
                amount,
                idempotencyKey
        );

        BankAccount sourceAccount =
                bankAccountService.getAccountForUser(
                        userId,
                        sourceAccountNumber
                );

        BankAccount destinationAccount =
                bankAccountService.getAccountByAccountNumber(
                        destinationAccountNumber
                );

        Payment payment = new Payment();

        payment.setAmount(amount);
        payment.setType(type);
        payment.setSourceAccount(sourceAccount);
        payment.setDestinationAccount(destinationAccount);
        payment.setDescription(description);
        payment.setIdempotencyKey(idempotencyKey);
        payment.setStatus(PaymentStatus.COMPLETED);

        return paymentRepository.save(payment);
    }

    private void validatePayment(
            String sourceAccountNumber,
            String destinationAccountNumber,
            BigDecimal amount,
            PaymentType type) {

        if (sourceAccountNumber == null ||
                sourceAccountNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Source account number is required"
            );
        }

        if (destinationAccountNumber == null ||
                destinationAccountNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Destination account number is required"
            );
        }

        if (sourceAccountNumber.equals(destinationAccountNumber)) {

            throw new IllegalArgumentException(
                    "Source and destination accounts must be different"
            );
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        if (type == null) {

            throw new IllegalArgumentException(
                    "Payment type is required"
            );
        }
    }

    private void validateIdempotentRetry(
            Payment existingPayment,
            String sourceAccountNumber,
            String destinationAccountNumber,
            BigDecimal amount,
            PaymentType type) {

        boolean sameRequest =
                existingPayment.getAmount().compareTo(amount) == 0
                        && existingPayment.getType() == type
                        && existingPayment.getSourceAccount()
                        .getAccountNumber()
                        .equals(sourceAccountNumber)
                        && existingPayment.getDestinationAccount()
                        .getAccountNumber()
                        .equals(destinationAccountNumber);

        if (!sameRequest) {

            throw new IllegalArgumentException(
                    "Idempotency key has already been used for a different payment"
            );
        }
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {

        if (idempotencyKey == null ||
                idempotencyKey.isBlank()) {

            return null;
        }

        return idempotencyKey.trim();
    }
}
