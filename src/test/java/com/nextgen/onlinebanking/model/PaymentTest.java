package com.nextgen.onlinebanking.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @Test
    void shouldCreatePaymentWithRequiredFields() {

        Payment payment = new Payment();

        payment.setAmount(new BigDecimal("250.00"));
        payment.setType(PaymentType.STANDARD);
        payment.setStatus(PaymentStatus.PENDING);

        assertEquals(
                new BigDecimal("250.00"),
                payment.getAmount());

        assertEquals(
                PaymentType.STANDARD,
                payment.getType());

        assertEquals(
                PaymentStatus.PENDING,
                payment.getStatus());
    }

    @Test
    void shouldGeneratePaymentReferenceBeforePersisting() {

        Payment payment = new Payment();

        assertNull(payment.getPaymentReference());

        payment.onCreate();

        assertNotNull(payment.getPaymentReference());
        assertFalse(payment.getPaymentReference().isBlank());
    }

    @Test
    void shouldSetCreatedAtBeforePersisting() {

        Payment payment = new Payment();

        assertNull(payment.getCreatedAt());

        payment.onCreate();

        assertNotNull(payment.getCreatedAt());
    }

    @Test
    void shouldDefaultStatusToPending() {

        Payment payment = new Payment();

        payment.onCreate();

        assertEquals(
                PaymentStatus.PENDING,
                payment.getStatus());
    }

    @Test
    void shouldSupportScheduledPayment() {

        Payment payment = new Payment();

        payment.setType(PaymentType.SCHEDULED);
        payment.setStatus(PaymentStatus.SCHEDULED);

        assertEquals(
                PaymentType.SCHEDULED,
                payment.getType());

        assertEquals(
                PaymentStatus.SCHEDULED,
                payment.getStatus());
    }

    @Test
    void shouldSupportQrPayment() {

        Payment payment = new Payment();

        payment.setType(PaymentType.QR);

        assertEquals(
                PaymentType.QR,
                payment.getType());
    }
}
