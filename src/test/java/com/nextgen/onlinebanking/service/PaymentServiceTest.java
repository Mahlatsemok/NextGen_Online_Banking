package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.Payment;
import com.nextgen.onlinebanking.model.PaymentStatus;
import com.nextgen.onlinebanking.model.PaymentType;
import com.nextgen.onlinebanking.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BankAccountService bankAccountService;

    @InjectMocks
    private PaymentService paymentService;

    private BankAccount sourceAccount;
    private BankAccount destinationAccount;

    @BeforeEach
    void setUp() {

        sourceAccount = new BankAccount();
        sourceAccount.setAccountNumber("ACC100001");

        destinationAccount = new BankAccount();
        destinationAccount.setAccountNumber("ACC100002");
    }

    @Test
    void shouldCreateStandardPaymentSuccessfully() {

        BigDecimal amount = new BigDecimal("250.00");

        when(bankAccountService.getAccountForUser(
                1L,
                "ACC100001"
        )).thenReturn(sourceAccount);

        when(bankAccountService.getAccountByAccountNumber(
                "ACC100002"
        )).thenReturn(destinationAccount);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.createPayment(
                1L,
                "ACC100001",
                "ACC100002",
                amount,
                PaymentType.STANDARD,
                "Test payment",
                "payment-key-001"
        );

        assertNotNull(result);
        assertEquals(amount, result.getAmount());
        assertEquals(PaymentType.STANDARD, result.getType());
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        assertEquals(sourceAccount, result.getSourceAccount());
        assertEquals(destinationAccount, result.getDestinationAccount());
        assertEquals("Test payment", result.getDescription());
        assertEquals("payment-key-001", result.getIdempotencyKey());

        verify(bankAccountService).transfer(
                1L,
                "ACC100001",
                "ACC100002",
                amount,
                "payment-key-001"
        );

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldRejectNullAmount() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.createPayment(
                                1L,
                                "ACC100001",
                                "ACC100002",
                                null,
                                PaymentType.STANDARD,
                                "Test payment",
                                null
                        )
                );

        assertEquals(
                "Payment amount must be greater than zero",
                exception.getMessage()
        );

        verifyNoInteractions(
                bankAccountService,
                paymentRepository
        );
    }

    @Test
    void shouldRejectZeroAmount() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.createPayment(
                                1L,
                                "ACC100001",
                                "ACC100002",
                                BigDecimal.ZERO,
                                PaymentType.STANDARD,
                                "Test payment",
                                null
                        )
                );

        assertEquals(
                "Payment amount must be greater than zero",
                exception.getMessage()
        );

        verifyNoInteractions(
                bankAccountService,
                paymentRepository
        );
    }

    @Test
    void shouldRejectNegativeAmount() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.createPayment(
                                1L,
                                "ACC100001",
                                "ACC100002",
                                new BigDecimal("-10.00"),
                                PaymentType.STANDARD,
                                "Test payment",
                                null
                        )
                );

        assertEquals(
                "Payment amount must be greater than zero",
                exception.getMessage()
        );

        verifyNoInteractions(
                bankAccountService,
                paymentRepository
        );
    }

    @Test
    void shouldRejectMissingSourceAccount() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.createPayment(
                                1L,
                                null,
                                "ACC100002",
                                new BigDecimal("100.00"),
                                PaymentType.STANDARD,
                                "Test payment",
                                null
                        )
                );

        assertEquals(
                "Source account number is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectBlankSourceAccount() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.createPayment(
                                1L,
                                "   ",
                                "ACC100002",
                                new BigDecimal("100.00"),
                                PaymentType.STANDARD,
                                "Test payment",
                                null
                        )
                );

        assertEquals(
                "Source account number is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectMissingDestinationAccount() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.createPayment(
                                1L,
                                "ACC100001",
                                null,
                                new BigDecimal("100.00"),
                                PaymentType.STANDARD,
                                "Test payment",
                                null
                        )
                );

        assertEquals(
                "Destination account number is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectBlankDestinationAccount() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.createPayment(
                                1L,
                                "ACC100001",
                                "   ",
                                new BigDecimal("100.00"),
                                PaymentType.STANDARD,
                                "Test payment",
                                null
                        )
                );

        assertEquals(
                "Destination account number is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectSameSourceAndDestinationAccount() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.createPayment(
                                1L,
                                "ACC100001",
                                "ACC100001",
                                new BigDecimal("100.00"),
                                PaymentType.STANDARD,
                                "Test payment",
                                null
                        )
                );

        assertEquals(
                "Source and destination accounts must be different",
                exception.getMessage()
        );

        verifyNoInteractions(
                bankAccountService,
                paymentRepository
        );
    }

    @Test
    void shouldRejectMissingPaymentType() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.createPayment(
                                1L,
                                "ACC100001",
                                "ACC100002",
                                new BigDecimal("100.00"),
                                null,
                                "Test payment",
                                null
                        )
                );

        assertEquals(
                "Payment type is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                bankAccountService,
                paymentRepository
        );
    }

    @Test
    void shouldRejectQrPaymentInStandardPaymentService() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.createPayment(
                                1L,
                                "ACC100001",
                                "ACC100002",
                                new BigDecimal("100.00"),
                                PaymentType.QR,
                                "QR payment",
                                null
                        )
                );

        assertEquals(
                "Only standard payments are supported",
                exception.getMessage()
        );

        verifyNoInteractions(
                bankAccountService,
                paymentRepository
        );
    }

    @Test
    void shouldRejectScheduledPaymentInStandardPaymentService() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.createPayment(
                                1L,
                                "ACC100001",
                                "ACC100002",
                                new BigDecimal("100.00"),
                                PaymentType.SCHEDULED,
                                "Scheduled payment",
                                null
                        )
                );

        assertEquals(
                "Only standard payments are supported",
                exception.getMessage()
        );

        verifyNoInteractions(
                bankAccountService,
                paymentRepository
        );
    }

    @Test
    void shouldReturnExistingPaymentForIdempotentRetry() {

        Payment existingPayment = new Payment();

        existingPayment.setAmount(new BigDecimal("250.00"));
        existingPayment.setType(PaymentType.STANDARD);
        existingPayment.setSourceAccount(sourceAccount);
        existingPayment.setDestinationAccount(destinationAccount);
        existingPayment.setIdempotencyKey("payment-key-001");
        existingPayment.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findByIdempotencyKey(
                "payment-key-001"
        )).thenReturn(Optional.of(existingPayment));

        Payment result = paymentService.createPayment(
                1L,
                "ACC100001",
                "ACC100002",
                new BigDecimal("250.00"),
                PaymentType.STANDARD,
                "Retry",
                "payment-key-001"
        );

        assertSame(existingPayment, result);

        verify(paymentRepository)
                .findByIdempotencyKey("payment-key-001");

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verifyNoInteractions(bankAccountService);
    }

    @Test
    void shouldRejectSameIdempotencyKeyForDifferentAmount() {

        Payment existingPayment = new Payment();

        existingPayment.setAmount(new BigDecimal("250.00"));
        existingPayment.setType(PaymentType.STANDARD);
        existingPayment.setSourceAccount(sourceAccount);
        existingPayment.setDestinationAccount(destinationAccount);
        existingPayment.setIdempotencyKey("payment-key-001");

        when(paymentRepository.findByIdempotencyKey(
                "payment-key-001"
        )).thenReturn(Optional.of(existingPayment));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.createPayment(
                                1L,
                                "ACC100001",
                                "ACC100002",
                                new BigDecimal("500.00"),
                                PaymentType.STANDARD,
                                "Different amount",
                                "payment-key-001"
                        )
                );

        assertEquals(
                "Idempotency key has already been used for a different payment",
                exception.getMessage()
        );

        verify(paymentRepository)
                .findByIdempotencyKey("payment-key-001");

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verifyNoInteractions(bankAccountService);
    }

    @Test
    void shouldRejectSameIdempotencyKeyForDifferentDestination() {

        Payment existingPayment = new Payment();

        existingPayment.setAmount(new BigDecimal("250.00"));
        existingPayment.setType(PaymentType.STANDARD);
        existingPayment.setSourceAccount(sourceAccount);
        existingPayment.setDestinationAccount(destinationAccount);
        existingPayment.setIdempotencyKey("payment-key-001");

        BankAccount anotherDestination = new BankAccount();
        anotherDestination.setAccountNumber("ACC100003");

        when(paymentRepository.findByIdempotencyKey(
                "payment-key-001"
        )).thenReturn(Optional.of(existingPayment));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.createPayment(
                                1L,
                                "ACC100001",
                                "ACC100003",
                                new BigDecimal("250.00"),
                                PaymentType.STANDARD,
                                "Different destination",
                                "payment-key-001"
                        )
                );

        assertEquals(
                "Idempotency key has already been used for a different payment",
                exception.getMessage()
        );

        verify(paymentRepository)
                .findByIdempotencyKey("payment-key-001");

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verifyNoInteractions(bankAccountService);
    }

    @Test
    void shouldNormalizeIdempotencyKeyBeforeSaving() {

        BigDecimal amount = new BigDecimal("100.00");

        when(bankAccountService.getAccountForUser(
                1L,
                "ACC100001"
        )).thenReturn(sourceAccount);

        when(bankAccountService.getAccountByAccountNumber(
                "ACC100002"
        )).thenReturn(destinationAccount);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.createPayment(
                1L,
                "ACC100001",
                "ACC100002",
                amount,
                PaymentType.STANDARD,
                "Whitespace key",
                "  payment-key-002  "
        );

        assertEquals(
                "payment-key-002",
                result.getIdempotencyKey()
        );

        verify(bankAccountService).transfer(
                1L,
                "ACC100001",
                "ACC100002",
                amount,
                "payment-key-002"
        );
    }

    @Test
    void shouldAllowPaymentWithoutIdempotencyKey() {

        BigDecimal amount = new BigDecimal("100.00");

        when(bankAccountService.getAccountForUser(
                1L,
                "ACC100001"
        )).thenReturn(sourceAccount);

        when(bankAccountService.getAccountByAccountNumber(
                "ACC100002"
        )).thenReturn(destinationAccount);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.createPayment(
                1L,
                "ACC100001",
                "ACC100002",
                amount,
                PaymentType.STANDARD,
                "No idempotency key",
                null
        );

        assertNotNull(result);
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        assertNull(result.getIdempotencyKey());

        verify(bankAccountService).transfer(
                1L,
                "ACC100001",
                "ACC100002",
                amount,
                null
        );

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldPropagateTransferFailure() {

        BigDecimal amount = new BigDecimal("1000.00");

        doThrow(new IllegalArgumentException("Insufficient funds"))
                .when(bankAccountService)
                .transfer(
                        1L,
                        "ACC100001",
                        "ACC100002",
                        amount,
                        "payment-key-003");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createPayment(
                        1L,
                        "ACC100001",
                        "ACC100002",
                        amount,
                        PaymentType.STANDARD,
                        "Payment",
                        "payment-key-003"));

        assertEquals(
                "Insufficient funds",
                exception.getMessage());

        verify(bankAccountService).transfer(
                1L,
                "ACC100001",
                "ACC100002",
                amount,
                "payment-key-003");

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }
    
        @Test
    void shouldCreateScheduledPaymentSuccessfully() {

        BigDecimal amount = new BigDecimal("250.00");
        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(1);

        when(bankAccountService.getAccountForUser(
                1L,
                "ACC100001"
        )).thenReturn(sourceAccount);

        when(bankAccountService.getAccountByAccountNumber(
                "ACC100002"
        )).thenReturn(destinationAccount);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.schedulePayment(
                1L,
                "ACC100001",
                "ACC100002",
                amount,
                scheduledAt,
                "Scheduled payment",
                "scheduled-key-001"
        );

        assertNotNull(result);
        assertEquals(amount, result.getAmount());
        assertEquals(PaymentType.SCHEDULED, result.getType());
        assertEquals(PaymentStatus.SCHEDULED, result.getStatus());
        assertEquals(sourceAccount, result.getSourceAccount());
        assertEquals(destinationAccount, result.getDestinationAccount());
        assertEquals("Scheduled payment", result.getDescription());
        assertEquals("scheduled-key-001", result.getIdempotencyKey());
        assertEquals(scheduledAt, result.getScheduledAt());

        verify(bankAccountService).getAccountForUser(
                1L,
                "ACC100001"
        );

        verify(bankAccountService).getAccountByAccountNumber(
                "ACC100002"
        );

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldCreateScheduledPaymentWithScheduledStatus() {

        LocalDateTime scheduledAt = LocalDateTime.now().plusHours(2);

        when(bankAccountService.getAccountForUser(
                1L,
                "ACC100001"
        )).thenReturn(sourceAccount);

        when(bankAccountService.getAccountByAccountNumber(
                "ACC100002"
        )).thenReturn(destinationAccount);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.schedulePayment(
                1L,
                "ACC100001",
                "ACC100002",
                new BigDecimal("100.00"),
                scheduledAt,
                "Future payment",
                null
        );

        assertEquals(PaymentStatus.SCHEDULED, result.getStatus());
    }

    @Test
    void shouldNotTransferMoneyWhenSchedulingPayment() {

        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(1);

        when(bankAccountService.getAccountForUser(
                1L,
                "ACC100001"
        )).thenReturn(sourceAccount);

        when(bankAccountService.getAccountByAccountNumber(
                "ACC100002"
        )).thenReturn(destinationAccount);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.schedulePayment(
                1L,
                "ACC100001",
                "ACC100002",
                new BigDecimal("500.00"),
                scheduledAt,
                "Future payment",
                "scheduled-key-002"
        );

        verify(bankAccountService, never()).transfer(
                anyLong(),
                anyString(),
                anyString(),
                any(BigDecimal.class),
                anyString()
        );

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldRejectScheduledPaymentInThePast() {

        LocalDateTime scheduledAt = LocalDateTime.now().minusMinutes(1);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.schedulePayment(
                                1L,
                                "ACC100001",
                                "ACC100002",
                                new BigDecimal("100.00"),
                                scheduledAt,
                                "Past payment",
                                null
                        )
                );

        assertEquals(
                "Scheduled date and time must be in the future",
                exception.getMessage()
        );

        verifyNoInteractions(
                bankAccountService,
                paymentRepository
        );
    }

    @Test
    void shouldRejectMissingScheduledAt() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.schedulePayment(
                                1L,
                                "ACC100001",
                                "ACC100002",
                                new BigDecimal("100.00"),
                                null,
                                "Missing scheduled date",
                                null
                        )
                );

        assertEquals(
                "Scheduled date and time is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                bankAccountService,
                paymentRepository
        );
    }

    @Test
    void shouldReturnExistingScheduledPaymentForIdempotentRetry() {

        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(1);

        Payment existingPayment = new Payment();

        existingPayment.setAmount(new BigDecimal("250.00"));
        existingPayment.setType(PaymentType.SCHEDULED);
        existingPayment.setSourceAccount(sourceAccount);
        existingPayment.setDestinationAccount(destinationAccount);
        existingPayment.setScheduledAt(scheduledAt);
        existingPayment.setIdempotencyKey("scheduled-key-003");
        existingPayment.setStatus(PaymentStatus.SCHEDULED);

        when(paymentRepository.findByIdempotencyKey(
                "scheduled-key-003"
        )).thenReturn(Optional.of(existingPayment));

        Payment result = paymentService.schedulePayment(
                1L,
                "ACC100001",
                "ACC100002",
                new BigDecimal("250.00"),
                scheduledAt,
                "Retry scheduled payment",
                "scheduled-key-003"
        );

        assertSame(existingPayment, result);

        verify(paymentRepository).findByIdempotencyKey(
                "scheduled-key-003"
        );

        verify(paymentRepository, never()).save(any(Payment.class));

        verifyNoInteractions(bankAccountService);
    }

    @Test
    void shouldRejectSameIdempotencyKeyForDifferentScheduledPayment() {

        LocalDateTime originalScheduledAt = LocalDateTime.now().plusDays(1);

        LocalDateTime differentScheduledAt = LocalDateTime.now().plusDays(2);

        Payment existingPayment = new Payment();

        existingPayment.setAmount(new BigDecimal("250.00"));
        existingPayment.setType(PaymentType.SCHEDULED);
        existingPayment.setSourceAccount(sourceAccount);
        existingPayment.setDestinationAccount(destinationAccount);
        existingPayment.setScheduledAt(originalScheduledAt);
        existingPayment.setIdempotencyKey("scheduled-key-004");
        existingPayment.setStatus(PaymentStatus.SCHEDULED);

        when(paymentRepository.findByIdempotencyKey(
                "scheduled-key-004")).thenReturn(Optional.of(existingPayment));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.schedulePayment(
                        1L,
                        "ACC100001",
                        "ACC100002",
                        new BigDecimal("250.00"),
                        differentScheduledAt,
                        "Different scheduled payment",
                        "scheduled-key-004"));

        assertEquals(
                "Idempotency key has already been used for a different scheduled payment",
                exception.getMessage());

        verify(paymentRepository).findByIdempotencyKey(
                "scheduled-key-004");

        verify(paymentRepository, never()).save(any(Payment.class));

        verifyNoInteractions(bankAccountService);
    }
    
}
