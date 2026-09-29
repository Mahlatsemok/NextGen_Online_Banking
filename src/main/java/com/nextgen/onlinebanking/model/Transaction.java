package com.nextgen.onlinebanking.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = "transaction_reference"),
        @UniqueConstraint(columnNames = "idempotency_key")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_reference", nullable = false, unique = true)
    private String transactionReference;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    //@ManyToOne(fetch = FetchType.LAZY, optional = false)
    //@JoinColumn(name = "source_account_id")
    //private BankAccount sourceAccount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private BankAccount sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private BankAccount destinationAccount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Transaction() {
    }

    @PrePersist
    protected void onCreate() {
        if (transactionReference == null) {
            transactionReference = UUID.randomUUID().toString();
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
       this.idempotencyKey = idempotencyKey;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BankAccount getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(BankAccount sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public BankAccount getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(BankAccount destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
