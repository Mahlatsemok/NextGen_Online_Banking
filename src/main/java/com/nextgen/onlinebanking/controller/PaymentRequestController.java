package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.dto.CreatePaymentRequest;
import com.nextgen.onlinebanking.model.PaymentRequest;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.service.PaymentRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-requests")
public class PaymentRequestController {

    private final PaymentRequestService paymentRequestService;

    public PaymentRequestController(
            PaymentRequestService paymentRequestService) {

        this.paymentRequestService = paymentRequestService;
    }

    @PostMapping
    public ResponseEntity<PaymentRequest> createRequest(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        PaymentRequest paymentRequest =
                paymentRequestService.createRequest(
                        user.getId(),
                        request.getRequesterAccountNumber(),
                        request.getRequestedFromAccountNumber(),
                        request.getAmount(),
                        request.getDescription());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentRequest);
    }

    @PostMapping("/{requestReference}/accept")
    public ResponseEntity<PaymentRequest> acceptRequest(
            @PathVariable String requestReference,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        PaymentRequest paymentRequest =
                paymentRequestService.acceptRequest(
                        user.getId(),
                        requestReference);

        return ResponseEntity.ok(paymentRequest);
    }

    @PostMapping("/{requestReference}/decline")
    public ResponseEntity<PaymentRequest> declineRequest(
            @PathVariable String requestReference,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        PaymentRequest paymentRequest =
                paymentRequestService.declineRequest(
                        user.getId(),
                        requestReference);

        return ResponseEntity.ok(paymentRequest);
    }

    @GetMapping("/{requestReference}")
    public ResponseEntity<PaymentRequest> getRequest(
            @PathVariable String requestReference,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        PaymentRequest paymentRequest =
                paymentRequestService.getRequestForUser(
                        user.getId(),
                        requestReference);

        return ResponseEntity.ok(paymentRequest);
    }
}
