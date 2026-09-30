package com.nextgen.onlinebanking.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payment_requests",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "request_reference")
        }
)
public class PaymentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "request_reference",
            nullable = false,
            unique = true
    )
    private String requestReference;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_account_id", nullable = false)
    private BankAccount requesterAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_from_account_id", nullable = false)
    private BankAccount requestedFromAccount;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime respondedAt;

    public PaymentRequest() {
    }

    @PrePersist
    protected void onCreate() {

        if (requestReference == null) {
            requestReference = UUID.randomUUID().toString();
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = PaymentRequestStatus.PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public String getRequestReference() {
        return requestReference;
    }

    public void setRequestReference(String requestReference) {
        this.requestReference = requestReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentRequestStatus status) {
        this.status = status;
    }

    public BankAccount getRequesterAccount() {
        return requesterAccount;
    }

    public void setRequesterAccount(BankAccount requesterAccount) {
        this.requesterAccount = requesterAccount;
    }

    public BankAccount getRequestedFromAccount() {
        return requestedFromAccount;
    }

    public void setRequestedFromAccount(BankAccount requestedFromAccount) {
        this.requestedFromAccount = requestedFromAccount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}
