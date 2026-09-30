package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.PaymentRequest;
import com.nextgen.onlinebanking.model.PaymentRequestStatus;
import com.nextgen.onlinebanking.repository.BankAccountRepository;
import com.nextgen.onlinebanking.repository.PaymentRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentRequestService {

    private final PaymentRequestRepository paymentRequestRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankAccountService bankAccountService;

    public PaymentRequestService(
            PaymentRequestRepository paymentRequestRepository,
            BankAccountRepository bankAccountRepository,
            BankAccountService bankAccountService) {

        this.paymentRequestRepository = paymentRequestRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.bankAccountService = bankAccountService;
    }

    /**
     * Creates a payment request.
     *
     * Creating a request does NOT move money.
     */
    @Transactional
    public PaymentRequest createRequest(
            Long requesterUserId,
            String requesterAccountNumber,
            String requestedFromAccountNumber,
            BigDecimal amount,
            String description) {

        validateAmount(amount);

        if (requesterAccountNumber == null
                || requesterAccountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Requester account number is required");
        }

        if (requestedFromAccountNumber == null
                || requestedFromAccountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Requested-from account number is required");
        }

        if (requesterAccountNumber.equals(requestedFromAccountNumber)) {
            throw new IllegalArgumentException(
                    "Requester and requested-from accounts must be different");
        }

        BankAccount requesterAccount = bankAccountService.getAccountForUser(
                requesterUserId,
                requesterAccountNumber);

        BankAccount requestedFromAccount = bankAccountService.getAccountByAccountNumber(
                requestedFromAccountNumber);

        PaymentRequest request = new PaymentRequest();

        request.setAmount(amount);
        request.setRequesterAccount(requesterAccount);
        request.setRequestedFromAccount(requestedFromAccount);
        request.setDescription(description);
        request.setStatus(PaymentRequestStatus.PENDING);

        return paymentRequestRepository.save(request);
    }

    public PaymentRequest getRequestForUser(
        Long userId,
            String requestReference) {

        PaymentRequest request = getRequestByReference(requestReference);

        boolean requesterOwnsRequest = request.getRequesterAccount()
                .getUser()
                .getId()
                .equals(userId);

        boolean requestedFromUserOwnsRequest = request.getRequestedFromAccount()
                .getUser()
                .getId()
                .equals(userId);

        if (!requesterOwnsRequest && !requestedFromUserOwnsRequest) {
            throw new IllegalArgumentException(
                    "Payment request does not belong to user");
        }

        return request;
    }


    /**
     * Accepts a pending payment request.
     *
     * Money is transferred from the requested-from account
     * to the requester account.
     */
    @Transactional
    public PaymentRequest acceptRequest(
            Long requestedFromUserId,
            String requestReference) {

        PaymentRequest request =
                getRequestByReference(requestReference);

        validatePending(request);

        BankAccount requestedFromAccount =
                request.getRequestedFromAccount();

        if (!requestedFromAccount.getUser()
                .getId()
                .equals(requestedFromUserId)) {

            throw new IllegalArgumentException(
                    "Payment request does not belong to user");
        }

        bankAccountService.transfer(
                requestedFromUserId,
                requestedFromAccount.getAccountNumber(),
                request.getRequesterAccount().getAccountNumber(),
                request.getAmount()
        );

        request.setStatus(PaymentRequestStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());

        return paymentRequestRepository.save(request);
    }

    /**
     * Declines a pending payment request.
     */
    @Transactional
    public PaymentRequest declineRequest(
            Long requestedFromUserId,
            String requestReference) {

        PaymentRequest request =
                getRequestByReference(requestReference);

        validatePending(request);

        BankAccount requestedFromAccount =
                request.getRequestedFromAccount();

        if (!requestedFromAccount.getUser()
                .getId()
                .equals(requestedFromUserId)) {

            throw new IllegalArgumentException(
                    "Payment request does not belong to user");
        }

        request.setStatus(PaymentRequestStatus.DECLINED);
        request.setRespondedAt(LocalDateTime.now());

        return paymentRequestRepository.save(request);
    }

    public PaymentRequest getRequestByReference(
            String requestReference) {

        if (requestReference == null
                || requestReference.isBlank()) {

            throw new IllegalArgumentException(
                    "Payment request reference is required");
        }

        return paymentRequestRepository
                .findByRequestReference(requestReference)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment request not found"));
    }

    public List<PaymentRequest> getRequestsCreatedByAccount(
            Long accountId) {

        return paymentRequestRepository
                .findByRequesterAccountId(accountId);
    }

    public List<PaymentRequest> getRequestsForAccount(
            Long accountId) {

        return paymentRequestRepository
                .findByRequestedFromAccountId(accountId);
    }

    public List<PaymentRequest> getPendingRequestsForAccount(
            Long accountId) {

        return paymentRequestRepository
                .findByRequestedFromAccountIdAndStatus(
                        accountId,
                        PaymentRequestStatus.PENDING);
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Payment request amount must be greater than zero");
        }
    }

    private void validatePending(PaymentRequest request) {

        if (request.getStatus()
                != PaymentRequestStatus.PENDING) {

            throw new IllegalArgumentException(
                    "Payment request has already been responded to");
        }
    }
}
