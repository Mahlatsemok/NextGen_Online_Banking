package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.Payment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class QrPaymentService {

    private static final String QR_PREFIX = "NEXTGEN:QR:";

    private final PaymentService paymentService;

    public QrPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public Payment createPayment(
            Long userId,
            String sourceAccountNumber,
            String qrCode,
            BigDecimal amount,
            String description,
            String idempotencyKey) {

        String destinationAccountNumber =
                decodeDestinationAccount(qrCode);

        return paymentService.createQrPayment(
                userId,
                sourceAccountNumber,
                destinationAccountNumber,
                amount,
                description,
                idempotencyKey
        );
    }

    String decodeDestinationAccount(String qrCode) {

        if (qrCode == null || qrCode.isBlank()) {
            throw new IllegalArgumentException(
                    "QR code is required"
            );
        }

        String normalizedCode = qrCode.trim();

        if (!normalizedCode.startsWith(QR_PREFIX)) {
            throw new IllegalArgumentException(
                    "Invalid QR payment code"
            );
        }

        String accountNumber =
                normalizedCode.substring(QR_PREFIX.length()).trim();

        if (accountNumber.isBlank()
                || accountNumber.contains(":")) {

            throw new IllegalArgumentException(
                    "Invalid QR payment code"
            );
        }

        return accountNumber;
    }
}
