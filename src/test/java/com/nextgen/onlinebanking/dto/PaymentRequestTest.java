package com.nextgen.onlinebanking.dto;

import com.nextgen.onlinebanking.model.PaymentType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void validPaymentRequestShouldHaveNoViolations() {

        PaymentRequest request = new PaymentRequest();

        request.setSourceAccountNumber("ACC100001");
        request.setDestinationAccountNumber("ACC100002");
        request.setAmount(new BigDecimal("250.00"));
        request.setType(PaymentType.STANDARD);
        request.setDescription("Electricity payment");
        request.setIdempotencyKey("payment-key-001");

        Set<ConstraintViolation<PaymentRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void destinationAccountNumberIsRequired() {

        PaymentRequest request = new PaymentRequest();

        request.setAmount(new BigDecimal("250.00"));
        request.setType(PaymentType.STANDARD);
        request.setIdempotencyKey("payment-key-001");

        Set<ConstraintViolation<PaymentRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage().equals(
                                "Destination account number is required"
                        ))
        );
    }

    @Test
    void blankDestinationAccountNumberShouldBeRejected() {

        PaymentRequest request = new PaymentRequest();

        request.setDestinationAccountNumber("   ");
        request.setAmount(new BigDecimal("250.00"));
        request.setType(PaymentType.STANDARD);
        request.setIdempotencyKey("payment-key-001");

        Set<ConstraintViolation<PaymentRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage().equals(
                                "Destination account number is required"
                        ))
        );
    }

    @Test
    void amountIsRequired() {

        PaymentRequest request = new PaymentRequest();

        request.setDestinationAccountNumber("ACC100002");
        request.setType(PaymentType.STANDARD);
        request.setIdempotencyKey("payment-key-001");

        Set<ConstraintViolation<PaymentRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage().equals(
                                "Payment amount is required"
                        ))
        );
    }

    @Test
    void zeroAmountShouldBeRejected() {

        PaymentRequest request = new PaymentRequest();

        request.setDestinationAccountNumber("ACC100002");
        request.setAmount(BigDecimal.ZERO);
        request.setType(PaymentType.STANDARD);
        request.setIdempotencyKey("payment-key-001");

        Set<ConstraintViolation<PaymentRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage().equals(
                                "Payment amount must be greater than zero"
                        ))
        );
    }

    @Test
    void negativeAmountShouldBeRejected() {

        PaymentRequest request = new PaymentRequest();

        request.setDestinationAccountNumber("ACC100002");
        request.setAmount(new BigDecimal("-10.00"));
        request.setType(PaymentType.STANDARD);
        request.setIdempotencyKey("payment-key-001");

        Set<ConstraintViolation<PaymentRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage().equals(
                                "Payment amount must be greater than zero"
                        ))
        );
    }

    @Test
    void paymentTypeIsRequired() {

        PaymentRequest request = new PaymentRequest();

        request.setDestinationAccountNumber("ACC100002");
        request.setAmount(new BigDecimal("250.00"));
        request.setIdempotencyKey("payment-key-001");

        Set<ConstraintViolation<PaymentRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage().equals(
                                "Payment type is required"
                        ))
        );
    }

    @Test
    void idempotencyKeyIsRequired() {

        PaymentRequest request = new PaymentRequest();

        request.setDestinationAccountNumber("ACC100002");
        request.setAmount(new BigDecimal("250.00"));
        request.setType(PaymentType.STANDARD);

        Set<ConstraintViolation<PaymentRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage().equals(
                                "Idempotency key is required"
                        ))
        );
    }

    @Test
    void blankIdempotencyKeyShouldBeRejected() {

        PaymentRequest request = new PaymentRequest();

        request.setDestinationAccountNumber("ACC100002");
        request.setAmount(new BigDecimal("250.00"));
        request.setType(PaymentType.STANDARD);
        request.setIdempotencyKey("   ");

        Set<ConstraintViolation<PaymentRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage().equals(
                                "Idempotency key is required"
                        ))
        );
    }
}
