package com.nextgen.onlinebanking.repository;

import com.nextgen.onlinebanking.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PaymentRequestRepositoryTest {

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    private BankAccount requesterAccount;
    private BankAccount requestedFromAccount;

    @BeforeEach
    void setUp() {

        User requester = new User();
        requester.setFirstName("Requester");
        requester.setLastName("User");
        requester.setEmail("requester@test.com");
        requester.setPassword("$2a$10$testpassword");
        requester = userRepository.save(requester);

        User recipient = new User();
        recipient.setFirstName("Recipient");
        recipient.setLastName("User");
        recipient.setEmail("recipient@test.com");
        recipient.setPassword("$2a$10$testpassword");
        recipient = userRepository.save(recipient);

        requesterAccount = new BankAccount();
        requesterAccount.setAccountNumber("ACC100001");
        requesterAccount.setUser(requester);
        requesterAccount.setAccountType(AccountType.CHECKING);
        requesterAccount.setBalance(new BigDecimal("1000.00"));
        requesterAccount = bankAccountRepository.save(requesterAccount);

        requestedFromAccount = new BankAccount();
        requestedFromAccount.setAccountNumber("ACC100002");
        requestedFromAccount.setUser(recipient);
        requestedFromAccount.setAccountType(AccountType.CHECKING);
        requestedFromAccount.setBalance(new BigDecimal("1000.00"));
        requestedFromAccount = bankAccountRepository.save(requestedFromAccount);
    }

    private PaymentRequest createRequest() {

        PaymentRequest request = new PaymentRequest();

        request.setAmount(new BigDecimal("250.00"));
        request.setRequesterAccount(requesterAccount);
        request.setRequestedFromAccount(requestedFromAccount);
        request.setDescription("Test payment request");

        return request;
    }

    @Test
    void shouldSaveAndFindPaymentRequestByReference() {

        PaymentRequest saved =
                paymentRequestRepository.save(createRequest());

        Optional<PaymentRequest> result =
                paymentRequestRepository.findByRequestReference(
                        saved.getRequestReference()
                );

        assertTrue(result.isPresent());
        assertEquals(
                saved.getId(),
                result.get().getId()
        );
        assertEquals(
                new BigDecimal("250.00"),
                result.get().getAmount()
        );
        assertEquals(
                PaymentRequestStatus.PENDING,
                result.get().getStatus()
        );
    }

    @Test
    void shouldFindRequestsByRequesterAccount() {

        PaymentRequest request =
                paymentRequestRepository.save(createRequest());

        List<PaymentRequest> results =
                paymentRequestRepository.findByRequesterAccountId(
                        requesterAccount.getId()
                );

        assertEquals(1, results.size());
        assertEquals(
                request.getId(),
                results.get(0).getId()
        );
    }

    @Test
    void shouldFindRequestsByRequestedFromAccount() {

        PaymentRequest request =
                paymentRequestRepository.save(createRequest());

        List<PaymentRequest> results =
                paymentRequestRepository.findByRequestedFromAccountId(
                        requestedFromAccount.getId()
                );

        assertEquals(1, results.size());
        assertEquals(
                request.getId(),
                results.get(0).getId()
        );
    }

    @Test
    void shouldFindPendingRequestsForRequestedFromAccount() {

        PaymentRequest pending =
                paymentRequestRepository.save(createRequest());

        PaymentRequest declined = createRequest();
        declined.setStatus(PaymentRequestStatus.DECLINED);
        paymentRequestRepository.save(declined);

        List<PaymentRequest> results =
                paymentRequestRepository
                        .findByRequestedFromAccountIdAndStatus(
                                requestedFromAccount.getId(),
                                PaymentRequestStatus.PENDING
                        );

        assertEquals(1, results.size());
        assertEquals(
                pending.getId(),
                results.get(0).getId()
        );
        assertEquals(
                PaymentRequestStatus.PENDING,
                results.get(0).getStatus()
        );
    }
}
