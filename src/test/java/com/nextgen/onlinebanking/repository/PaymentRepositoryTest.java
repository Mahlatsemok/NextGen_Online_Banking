package com.nextgen.onlinebanking.repository;

import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.Payment;
import com.nextgen.onlinebanking.model.PaymentStatus;
import com.nextgen.onlinebanking.model.PaymentType;
import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindPaymentById() {

        BankAccount account = createAccount();

        Payment payment = createPayment(account);

        Payment saved = paymentRepository.save(payment);

        assertNotNull(saved.getId());

        Optional<Payment> found =
                paymentRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(
                saved.getId(),
                found.get().getId());
    }

    @Test
    void shouldFindPaymentByPaymentReference() {

        BankAccount account = createAccount();

        Payment payment = createPayment(account);

        Payment saved = paymentRepository.save(payment);

        Optional<Payment> found =
                paymentRepository.findByPaymentReference(
                        saved.getPaymentReference());

        assertTrue(found.isPresent());

        assertEquals(
                saved.getPaymentReference(),
                found.get().getPaymentReference());
    }

    @Test
    void shouldFindPaymentByIdempotencyKey() {

        BankAccount account = createAccount();

        Payment payment = createPayment(account);

        payment.setIdempotencyKey("payment-key-123");

        paymentRepository.save(payment);

        Optional<Payment> found =
                paymentRepository.findByIdempotencyKey(
                        "payment-key-123");

        assertTrue(found.isPresent());

        assertEquals(
                "payment-key-123",
                found.get().getIdempotencyKey());
    }

    @Test
    void shouldFindPaymentsBySourceAccount() {

        BankAccount account = createAccount();

        Payment payment = createPayment(account);

        paymentRepository.save(payment);

        var payments =
                paymentRepository.findBySourceAccountId(
                        account.getId());

        assertEquals(1, payments.size());
        assertEquals(
                payment.getPaymentReference(),
                payments.get(0).getPaymentReference());
    }

    @Test
    void shouldFindPaymentsByStatus() {

        BankAccount account = createAccount();

        Payment payment = createPayment(account);

        payment.setStatus(PaymentStatus.PENDING);

        paymentRepository.save(payment);

        var payments =
                paymentRepository.findByStatus(
                        PaymentStatus.PENDING);

        assertEquals(1, payments.size());
    }

    @Test
    void shouldFindDueScheduledPayments() {

        BankAccount account = createAccount();

        Payment payment = createPayment(account);

        payment.setType(PaymentType.SCHEDULED);
        payment.setStatus(PaymentStatus.SCHEDULED);
        payment.setScheduledAt(
                LocalDateTime.now().minusMinutes(5));

        paymentRepository.save(payment);

        var payments =
                paymentRepository
                        .findByStatusAndScheduledAtLessThanEqual(
                                PaymentStatus.SCHEDULED,
                                LocalDateTime.now());

        assertEquals(1, payments.size());
    }

    @Test
    void shouldCheckWhetherPaymentReferenceExists() {

        BankAccount account = createAccount();

        Payment payment = createPayment(account);

        Payment saved = paymentRepository.save(payment);

        assertTrue(
                paymentRepository.existsByPaymentReference(
                        saved.getPaymentReference()));
    }

    @Test
    void shouldCheckWhetherIdempotencyKeyExists() {

        BankAccount account = createAccount();

        Payment payment = createPayment(account);

        payment.setIdempotencyKey("unique-payment-key");

        paymentRepository.save(payment);

        assertTrue(
                paymentRepository.existsByIdempotencyKey(
                        "unique-payment-key"));
    }

    private BankAccount createAccount() {

        User user = new User();

        user.setFirstName("Payment");
        user.setLastName("Test");
        user.setEmail("payment-" + UUID.randomUUID() + "@example.com");
        user.setPassword("password123");

        User savedUser = userRepository.save(user);

        BankAccount account = new BankAccount();

        account.setAccountNumber("ACC-" + UUID.randomUUID());
        account.setUser(savedUser);
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("1000.00"));

        return bankAccountRepository.save(account);
    }


    private Payment createPayment(
            BankAccount account) {

        Payment payment = new Payment();

        payment.setAmount(
                new BigDecimal("100.00"));

        payment.setType(
                PaymentType.STANDARD);

        payment.setStatus(
                PaymentStatus.PENDING);

        payment.setSourceAccount(account);

        return payment;
    }
}
