package com.nextgen.onlinebanking.dto;

import com.nextgen.onlinebanking.model.Payment;
import com.nextgen.onlinebanking.model.PaymentStatus;
import com.nextgen.onlinebanking.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private String paymentReference;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentType type;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private String description;
    private String idempotencyKey;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;

    public PaymentResponse(
            Long id,
            String paymentReference,
            BigDecimal amount,
            PaymentStatus status,
            PaymentType type,
            String sourceAccountNumber,
            String destinationAccountNumber,
            String description,
            String idempotencyKey,
            LocalDateTime scheduledAt,
            LocalDateTime createdAt) {

        this.id = id;
        this.paymentReference = paymentReference;
        this.amount = amount;
        this.status = status;
        this.type = type;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
        this.scheduledAt = scheduledAt;
        this.createdAt = createdAt;
    }

    public static PaymentResponse fromPayment(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getType(),
                payment.getSourceAccount().getAccountNumber(),
                payment.getDestinationAccount() != null
                        ? payment.getDestinationAccount().getAccountNumber()
                        : null,
                payment.getDescription(),
                payment.getIdempotencyKey(),
                payment.getScheduledAt(),
                payment.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentType getType() {
        return type;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public String getDescription() {
        return description;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
