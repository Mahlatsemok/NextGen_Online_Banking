package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.dto.PaymentRequest;
import com.nextgen.onlinebanking.dto.PaymentResponse;
import com.nextgen.onlinebanking.dto.ScheduledPaymentRequest;
import com.nextgen.onlinebanking.model.Payment;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        Payment payment = paymentService.createPayment(
                user.getId(),
                request.getSourceAccountNumber(),
                request.getDestinationAccountNumber(),
                request.getAmount(),
                request.getType(),
                request.getDescription(),
                request.getIdempotencyKey());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PaymentResponse.fromPayment(payment));
    }

    @PostMapping("/scheduled")
    public ResponseEntity<PaymentResponse> schedulePayment(
            @Valid @RequestBody ScheduledPaymentRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        Payment payment = paymentService.schedulePayment(
                user.getId(),
                request.getSourceAccountNumber(),
                request.getDestinationAccountNumber(),
                request.getAmount(),
                request.getScheduledAt(),
                request.getDescription(),
                request.getIdempotencyKey());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PaymentResponse.fromPayment(payment));
    }
}
