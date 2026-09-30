package com.nextgen.onlinebanking.dto;

import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.Payment;
import com.nextgen.onlinebanking.model.PaymentStatus;
import com.nextgen.onlinebanking.model.PaymentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentResponseTest {

    @Test
    void shouldCreateResponseFromPayment() {

        BankAccount sourceAccount = new BankAccount();
        sourceAccount.setAccountNumber("ACC100001");

        BankAccount destinationAccount = new BankAccount();
        destinationAccount.setAccountNumber("ACC100002");

        Payment payment = new Payment();

        payment.setAmount(new BigDecimal("250.00"));
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setType(PaymentType.STANDARD);
        payment.setSourceAccount(sourceAccount);
        payment.setDestinationAccount(destinationAccount);
        payment.setDescription("Test payment");
        payment.setIdempotencyKey("payment-key-001");

        PaymentResponse response =
                PaymentResponse.fromPayment(payment);

        assertEquals(
                payment.getPaymentReference(),
                response.getPaymentReference()
        );

        assertEquals(
                new BigDecimal("250.00"),
                response.getAmount()
        );

        assertEquals(
                PaymentStatus.COMPLETED,
                response.getStatus()
        );

        assertEquals(
                PaymentType.STANDARD,
                response.getType()
        );

        assertEquals(
                "ACC100001",
                response.getSourceAccountNumber()
        );

        assertEquals(
                "ACC100002",
                response.getDestinationAccountNumber()
        );

        assertEquals(
                "Test payment",
                response.getDescription()
        );

        assertEquals(
                "payment-key-001",
                response.getIdempotencyKey()
        );
    }
}
