package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.dto.CreatePaymentRequest;
import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.PaymentRequest;
import com.nextgen.onlinebanking.model.PaymentRequestStatus;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.service.PaymentRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentRequestControllerTest {

    private final PaymentRequestService paymentRequestService =
            mock(PaymentRequestService.class);

    private final Authentication authentication =
            mock(Authentication.class);

    private final PaymentRequestController paymentRequestController =
            new PaymentRequestController(paymentRequestService);

    @Test
    void shouldCreatePaymentRequestForAuthenticatedUser() {

        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setRequesterAccountNumber("1000000001");
        request.setRequestedFromAccountNumber("1000000002");
        request.setAmount(new BigDecimal("250.00"));
        request.setDescription("Payment request");

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("250.00"));
        paymentRequest.setDescription("Payment request");
        paymentRequest.setStatus(PaymentRequestStatus.PENDING);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(paymentRequestService.createRequest(
                userId,
                request.getRequesterAccountNumber(),
                request.getRequestedFromAccountNumber(),
                request.getAmount(),
                request.getDescription()))
                .thenReturn(paymentRequest);

        var response = paymentRequestController.createRequest(
                request,
                authentication);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                new BigDecimal("250.00"),
                response.getBody().getAmount());

        assertEquals(
                PaymentRequestStatus.PENDING,
                response.getBody().getStatus());

        verify(paymentRequestService)
                .createRequest(
                        userId,
                        "1000000001",
                        "1000000002",
                        new BigDecimal("250.00"),
                        "Payment request");
    }

    @Test
    void shouldAcceptPaymentRequestForAuthenticatedUser() {

        Long userId = 2L;
        String requestReference = "REQ-123";

        User user = new User();
        user.setId(userId);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setStatus(PaymentRequestStatus.ACCEPTED);
        paymentRequest.setAmount(new BigDecimal("250.00"));

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(paymentRequestService.acceptRequest(
                userId,
                requestReference))
                .thenReturn(paymentRequest);

        var response = paymentRequestController.acceptRequest(
                requestReference,
                authentication);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                PaymentRequestStatus.ACCEPTED,
                response.getBody().getStatus());

        verify(paymentRequestService)
                .acceptRequest(userId, requestReference);
    }

    @Test
    void shouldDeclinePaymentRequestForAuthenticatedUser() {

        Long userId = 2L;
        String requestReference = "REQ-123";

        User user = new User();
        user.setId(userId);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setStatus(PaymentRequestStatus.DECLINED);
        paymentRequest.setAmount(new BigDecimal("250.00"));

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(paymentRequestService.declineRequest(
                userId,
                requestReference))
                .thenReturn(paymentRequest);

        var response = paymentRequestController.declineRequest(
                requestReference,
                authentication);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                PaymentRequestStatus.DECLINED,
                response.getBody().getStatus());

        verify(paymentRequestService)
                .declineRequest(userId, requestReference);
    }

    @Test
    void shouldReturnPaymentRequestForAuthenticatedUser() {

        Long userId = 1L;
        String requestReference = "REQ-123";

        User user = new User();
        user.setId(userId);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("250.00"));
        paymentRequest.setStatus(PaymentRequestStatus.PENDING);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(paymentRequestService.getRequestForUser(
                userId,
                requestReference))
                .thenReturn(paymentRequest);

        var response = paymentRequestController.getRequest(
                requestReference,
                authentication);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                new BigDecimal("250.00"),
                response.getBody().getAmount());

        assertEquals(
                PaymentRequestStatus.PENDING,
                response.getBody().getStatus());

        verify(paymentRequestService)
                .getRequestForUser(
                        userId,
                        requestReference);
    }
}
