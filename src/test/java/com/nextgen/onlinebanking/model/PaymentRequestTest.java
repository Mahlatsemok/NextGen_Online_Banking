package com.nextgen.onlinebanking.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestTest {

    @Test
    void shouldCreatePaymentRequestWithExpectedValues() {

        BankAccount requester = new BankAccount();
        requester.setAccountNumber("ACC100001");

        BankAccount requestedFrom = new BankAccount();
        requestedFrom.setAccountNumber("ACC100002");

        PaymentRequest request = new PaymentRequest();

        request.setAmount(new BigDecimal("500.00"));
        request.setRequesterAccount(requester);
        request.setRequestedFromAccount(requestedFrom);
        request.setDescription("Money requested");

        request.onCreate();

        assertNotNull(request.getRequestReference());
        assertEquals(new BigDecimal("500.00"), request.getAmount());
        assertEquals(PaymentRequestStatus.PENDING, request.getStatus());
        assertEquals(requester, request.getRequesterAccount());
        assertEquals(requestedFrom, request.getRequestedFromAccount());
        assertEquals("Money requested", request.getDescription());
        assertNotNull(request.getCreatedAt());
        assertNull(request.getRespondedAt());
    }

    @Test
    void shouldGenerateUniqueRequestReferences() {

        PaymentRequest first = new PaymentRequest();
        PaymentRequest second = new PaymentRequest();

        first.onCreate();
        second.onCreate();

        assertNotNull(first.getRequestReference());
        assertNotNull(second.getRequestReference());
        assertNotEquals(
                first.getRequestReference(),
                second.getRequestReference()
        );
    }

    @Test
    void shouldAllowStatusChange() {

        PaymentRequest request = new PaymentRequest();

        request.setStatus(PaymentRequestStatus.ACCEPTED);

        assertEquals(
                PaymentRequestStatus.ACCEPTED,
                request.getStatus()
        );
    }

    @Test
    void shouldAllowRespondedAtToBeSet() {

        PaymentRequest request = new PaymentRequest();

        var respondedAt =
                java.time.LocalDateTime.now();

        request.setRespondedAt(respondedAt);

        assertEquals(respondedAt, request.getRespondedAt());
    }
}
