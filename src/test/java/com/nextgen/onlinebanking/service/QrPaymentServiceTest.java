package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QrPaymentServiceTest {

    @Mock
    private PaymentService paymentService;

    @Test
    void shouldDecodeDestinationAccountFromValidQrCode() {

        QrPaymentService service =
                new QrPaymentService(paymentService);

        assertEquals(
                "ACC100002",
                service.decodeDestinationAccount(
                        "NEXTGEN:QR:ACC100002"
                )
        );
    }

    @Test
    void shouldTrimQrCodeBeforeDecoding() {

        QrPaymentService service =
                new QrPaymentService(paymentService);

        assertEquals(
                "ACC100002",
                service.decodeDestinationAccount(
                        "  NEXTGEN:QR:ACC100002  "
                )
        );
    }

    @Test
    void shouldRejectMissingQrCode() {

        QrPaymentService service =
                new QrPaymentService(paymentService);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.decodeDestinationAccount(" ")
                );

        assertEquals(
                "QR code is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectInvalidQrCodeFormat() {

        QrPaymentService service =
                new QrPaymentService(paymentService);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.decodeDestinationAccount(
                                        "INVALID:ACC100002"
                                )
                );

        assertEquals(
                "Invalid QR payment code",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectQrCodeWithoutDestinationAccount() {

        QrPaymentService service =
                new QrPaymentService(paymentService);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.decodeDestinationAccount(
                                        "NEXTGEN:QR:"
                                )
                );

        assertEquals(
                "Invalid QR payment code",
                exception.getMessage()
        );
    }

    @Test
    void shouldDelegateValidQrPaymentToPaymentService() {

        QrPaymentService service =
                new QrPaymentService(paymentService);

        Payment payment = new Payment();

        when(paymentService.createQrPayment(
                1L,
                "ACC100001",
                "ACC100002",
                new BigDecimal("250.00"),
                "QR payment",
                "qr-key-001"
        )).thenReturn(payment);

        Payment result = service.createPayment(
                1L,
                "ACC100001",
                "NEXTGEN:QR:ACC100002",
                new BigDecimal("250.00"),
                "QR payment",
                "qr-key-001"
        );

        assertSame(payment, result);

        verify(paymentService).createQrPayment(
                1L,
                "ACC100001",
                "ACC100002",
                new BigDecimal("250.00"),
                "QR payment",
                "qr-key-001"
        );
    }
}
