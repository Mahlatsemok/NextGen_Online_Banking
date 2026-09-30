package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.PaymentRequest;
import com.nextgen.onlinebanking.model.PaymentRequestStatus;
import com.nextgen.onlinebanking.repository.BankAccountRepository;
import com.nextgen.onlinebanking.repository.PaymentRequestRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRequestServiceTest {

    @Mock
    private PaymentRequestRepository paymentRequestRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankAccountService bankAccountService;

    @InjectMocks
    private PaymentRequestService paymentRequestService;

    private BankAccount requesterAccount;
    private BankAccount requestedFromAccount;

    @BeforeEach
    void setUp() {
        requesterAccount = mock(BankAccount.class);
        requestedFromAccount = mock(BankAccount.class);
    }

    @Test
    void shouldCreatePendingPaymentRequest() {
        Long requesterUserId = 1L;
        String requesterAccountNumber = "ACC-001";
        String requestedFromAccountNumber = "ACC-002";
        BigDecimal amount = new BigDecimal("250.00");

        when(bankAccountService.getAccountForUser(
                requesterUserId,
                requesterAccountNumber))
                .thenReturn(requesterAccount);

        when(bankAccountService.getAccountByAccountNumber(
                requestedFromAccountNumber))
                .thenReturn(requestedFromAccount);

        when(paymentRequestRepository.save(any(PaymentRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentRequest result =
                paymentRequestService.createRequest(
                        requesterUserId,
                        requesterAccountNumber,
                        requestedFromAccountNumber,
                        amount,
                        "Dinner");

        assertNotNull(result);
        assertEquals(amount, result.getAmount());
        assertEquals(PaymentRequestStatus.PENDING, result.getStatus());
        assertEquals(requesterAccount, result.getRequesterAccount());
        assertEquals(requestedFromAccount, result.getRequestedFromAccount());
        assertEquals("Dinner", result.getDescription());

        verify(paymentRequestRepository).save(any(PaymentRequest.class));

        verify(bankAccountService)
                .getAccountForUser(
                        requesterUserId,
                        requesterAccountNumber);

        verify(bankAccountService)
                .getAccountByAccountNumber(
                        requestedFromAccountNumber);
    }

    @Test
    void shouldRejectNullAmount() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentRequestService.createRequest(
                                1L,
                                "ACC-001",
                                "ACC-002",
                                null,
                                "Test"));

        assertEquals(
                "Payment request amount must be greater than zero",
                exception.getMessage());

        verifyNoInteractions(
                paymentRequestRepository,
                bankAccountService,
                bankAccountRepository);
    }

    @Test
    void shouldRejectZeroAmount() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentRequestService.createRequest(
                                1L,
                                "ACC-001",
                                "ACC-002",
                                BigDecimal.ZERO,
                                "Test"));

        assertEquals(
                "Payment request amount must be greater than zero",
                exception.getMessage());

        verifyNoInteractions(
                paymentRequestRepository,
                bankAccountService,
                bankAccountRepository);
    }

    @Test
    void shouldRejectNegativeAmount() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentRequestService.createRequest(
                                1L,
                                "ACC-001",
                                "ACC-002",
                                new BigDecimal("-50.00"),
                                "Test"));

        assertEquals(
                "Payment request amount must be greater than zero",
                exception.getMessage());

        verifyNoInteractions(
                paymentRequestRepository,
                bankAccountService,
                bankAccountRepository);
    }

    @Test
    void shouldRejectMissingRequesterAccountNumber() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentRequestService.createRequest(
                                1L,
                                null,
                                "ACC-002",
                                new BigDecimal("100.00"),
                                "Test"));

        assertEquals(
                "Requester account number is required",
                exception.getMessage());

        verifyNoInteractions(
                paymentRequestRepository,
                bankAccountService,
                bankAccountRepository);
    }

    @Test
    void shouldRejectMissingRequestedFromAccountNumber() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentRequestService.createRequest(
                                1L,
                                "ACC-001",
                                null,
                                new BigDecimal("100.00"),
                                "Test"));

        assertEquals(
                "Requested-from account number is required",
                exception.getMessage());

        verifyNoInteractions(
                paymentRequestRepository,
                bankAccountService,
                bankAccountRepository);
    }

    @Test
    void shouldRejectSameRequesterAndRequestedFromAccount() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentRequestService.createRequest(
                                1L,
                                "ACC-001",
                                "ACC-001",
                                new BigDecimal("100.00"),
                                "Test"));

        assertEquals(
                "Requester and requested-from accounts must be different",
                exception.getMessage());

        verifyNoInteractions(
                paymentRequestRepository,
                bankAccountService,
                bankAccountRepository);
    }

    @Test
    void shouldAcceptPendingRequestAndTransferMoney() {
        Long requestedFromUserId = 2L;
        String requestReference = "REQ-001";

        PaymentRequest request = new PaymentRequest();

        request.setAmount(new BigDecimal("300.00"));
        request.setStatus(PaymentRequestStatus.PENDING);
        request.setRequesterAccount(requesterAccount);
        request.setRequestedFromAccount(requestedFromAccount);

        when(paymentRequestRepository
                .findByRequestReference(requestReference))
                .thenReturn(Optional.of(request));

        //when(requestedFromAccount.getUser()).thenReturn(
        //      mockUserWithId(2L));
          
        com.nextgen.onlinebanking.model.User requestedFromUser =
                mockUserWithId(2L);

        when(requestedFromAccount.getUser())
                .thenReturn(requestedFromUser);

        when(requestedFromAccount.getAccountNumber())
                .thenReturn("ACC-002");

        when(requesterAccount.getAccountNumber())
                .thenReturn("ACC-001");

        when(paymentRequestRepository.save(request))
                .thenReturn(request);

        PaymentRequest result =
                paymentRequestService.acceptRequest(
                        requestedFromUserId,
                        requestReference);

        assertEquals(
                PaymentRequestStatus.ACCEPTED,
                result.getStatus());

        assertNotNull(result.getRespondedAt());

        verify(bankAccountService).transfer(
                2L,
                "ACC-002",
                "ACC-001",
                new BigDecimal("300.00"));

        verify(paymentRequestRepository).save(request);
    }

    @Test
    void shouldRejectAcceptWhenRequestIsNotPending() {
        PaymentRequest request = new PaymentRequest();

        request.setStatus(PaymentRequestStatus.ACCEPTED);

        when(paymentRequestRepository
                .findByRequestReference("REQ-001"))
                .thenReturn(Optional.of(request));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentRequestService.acceptRequest(
                                2L,
                                "REQ-001"));

        assertEquals(
                "Payment request has already been responded to",
                exception.getMessage());

        verify(bankAccountService, never())
                .transfer(anyLong(), anyString(), anyString(), any());

        verify(paymentRequestRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectAcceptWhenUserDoesNotOwnRequestedFromAccount() {
        PaymentRequest request = new PaymentRequest();

        request.setStatus(PaymentRequestStatus.PENDING);
        request.setRequestedFromAccount(requestedFromAccount);

        when(paymentRequestRepository
                .findByRequestReference("REQ-001"))
                .thenReturn(Optional.of(request));

        //when(requestedFromAccount.getUser()).thenReturn(
        //      mockUserWithId(99L));
          
        com.nextgen.onlinebanking.model.User requestedFromUser =
                mockUserWithId(99L);
        

        when(requestedFromAccount.getUser())
                .thenReturn(requestedFromUser);
        

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentRequestService.acceptRequest(
                                2L,
                                "REQ-001"));

        assertEquals(
                "Payment request does not belong to user",
                exception.getMessage());

        verify(bankAccountService, never())
                .transfer(anyLong(), anyString(), anyString(), any());

        verify(paymentRequestRepository, never())
                .save(any());
    }

    @Test
    void shouldDeclinePendingRequest() {
        PaymentRequest request = new PaymentRequest();

        request.setStatus(PaymentRequestStatus.PENDING);
        request.setRequestedFromAccount(requestedFromAccount);

        when(paymentRequestRepository
                .findByRequestReference("REQ-001"))
                .thenReturn(Optional.of(request));

       // when(requestedFromAccount.getUser()).thenReturn(
       //mockUserWithId(2L));
        com.nextgen.onlinebanking.model.User requestedFromUser =
                mockUserWithId(2L);

        when(requestedFromAccount.getUser())
                .thenReturn(requestedFromUser);


        when(paymentRequestRepository.save(request))
                .thenReturn(request);

        PaymentRequest result =
                paymentRequestService.declineRequest(
                        2L,
                        "REQ-001");

        assertEquals(
                PaymentRequestStatus.DECLINED,
                result.getStatus());

        assertNotNull(result.getRespondedAt());

        verify(bankAccountService, never())
                .transfer(anyLong(), anyString(), anyString(), any());

        verify(paymentRequestRepository).save(request);
    }

    @Test
    void shouldRejectDeclineWhenRequestIsNotPending() {
        PaymentRequest request = new PaymentRequest();

        request.setStatus(PaymentRequestStatus.DECLINED);

        when(paymentRequestRepository
                .findByRequestReference("REQ-001"))
                .thenReturn(Optional.of(request));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentRequestService.declineRequest(
                                2L,
                                "REQ-001"));

        assertEquals(
                "Payment request has already been responded to",
                exception.getMessage());

        verify(paymentRequestRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectDeclineWhenUserDoesNotOwnRequestedFromAccount() {
        PaymentRequest request = new PaymentRequest();

        request.setStatus(PaymentRequestStatus.PENDING);
        request.setRequestedFromAccount(requestedFromAccount);

        when(paymentRequestRepository
                .findByRequestReference("REQ-001"))
                .thenReturn(Optional.of(request));

        //when(requestedFromAccount.getUser()).thenReturn(
        //      mockUserWithId(99L));
          
        com.nextgen.onlinebanking.model.User requestedFromUser =
                mockUserWithId(99L);

        when(requestedFromAccount.getUser())
                .thenReturn(requestedFromUser);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentRequestService.declineRequest(
                                2L,
                                "REQ-001"));

        assertEquals(
                "Payment request does not belong to user",
                exception.getMessage());

        verify(paymentRequestRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectMissingRequestReference() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentRequestService
                                .getRequestByReference(null));

        assertEquals(
                "Payment request reference is required",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankRequestReference() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentRequestService
                                .getRequestByReference("   "));

        assertEquals(
                "Payment request reference is required",
                exception.getMessage());
    }

    @Test
    void shouldRejectUnknownRequestReference() {
        when(paymentRequestRepository
                .findByRequestReference("UNKNOWN"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentRequestService
                                .getRequestByReference("UNKNOWN"));

        assertEquals(
                "Payment request not found",
                exception.getMessage());
    }

    private com.nextgen.onlinebanking.model.User mockUserWithId(Long id) {
        com.nextgen.onlinebanking.model.User user =
                mock(com.nextgen.onlinebanking.model.User.class);

        when(user.getId()).thenReturn(id);

        return user;
    }
}
