package com.nextgen.onlinebanking.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QrPaymentRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator =
                Validation
                        .buildDefaultValidatorFactory()
                        .getValidator();
    }

    @Test
    void validQrPaymentRequestShouldHaveNoViolations() {

        QrPaymentRequest request = new QrPaymentRequest();

        request.setSourceAccountNumber("ACC100001");
        request.setQrCode("NEXTGEN:QR:ACC100002");
        request.setAmount(new BigDecimal("250.00"));
        request.setDescription("QR payment");
        request.setIdempotencyKey("qr-key-001");

        assertTrue(
                validator.validate(request).isEmpty()
        );
    }

    @Test
    void missingQrCodeShouldBeRejected() {

        QrPaymentRequest request = new QrPaymentRequest();

        request.setSourceAccountNumber("ACC100001");
        request.setAmount(new BigDecimal("250.00"));
        request.setIdempotencyKey("qr-key-001");

        assertTrue(
                validator.validate(request)
                        .stream()
                        .anyMatch(v ->
                                v.getMessage().equals(
                                        "QR code is required"
                                ))
        );
    }

    @Test
    void zeroAmountShouldBeRejected() {

        QrPaymentRequest request = new QrPaymentRequest();

        request.setSourceAccountNumber("ACC100001");
        request.setQrCode("NEXTGEN:QR:ACC100002");
        request.setAmount(BigDecimal.ZERO);
        request.setIdempotencyKey("qr-key-001");

        assertTrue(
                validator.validate(request)
                        .stream()
                        .anyMatch(v ->
                                v.getMessage().equals(
                                        "Payment amount must be greater than zero"
                                ))
        );
    }
}
