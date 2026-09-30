package com.nextgen.onlinebanking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreatePaymentRequest {

    @NotBlank(message = "Requester account number is required")
    private String requesterAccountNumber;

    @NotBlank(message = "Requested-from account number is required")
    private String requestedFromAccountNumber;

    @NotNull(message = "Payment request amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Payment request amount must be greater than zero"
    )
    private BigDecimal amount;

    private String description;

    public CreatePaymentRequest() {
    }

    public String getRequesterAccountNumber() {
        return requesterAccountNumber;
    }

    public void setRequesterAccountNumber(String requesterAccountNumber) {
        this.requesterAccountNumber = requesterAccountNumber;
    }

    public String getRequestedFromAccountNumber() {
        return requestedFromAccountNumber;
    }

    public void setRequestedFromAccountNumber(String requestedFromAccountNumber) {
        this.requestedFromAccountNumber = requestedFromAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
