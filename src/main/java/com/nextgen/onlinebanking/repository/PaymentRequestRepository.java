package com.nextgen.onlinebanking.repository;

import com.nextgen.onlinebanking.model.PaymentRequest;
import com.nextgen.onlinebanking.model.PaymentRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRequestRepository
        extends JpaRepository<PaymentRequest, Long> {

    Optional<PaymentRequest> findByRequestReference(
            String requestReference
    );

    List<PaymentRequest> findByRequesterAccountId(
            Long accountId
    );

    List<PaymentRequest> findByRequestedFromAccountId(
            Long accountId
    );

    List<PaymentRequest> findByRequestedFromAccountIdAndStatus(
            Long accountId,
            PaymentRequestStatus status
    );
}
