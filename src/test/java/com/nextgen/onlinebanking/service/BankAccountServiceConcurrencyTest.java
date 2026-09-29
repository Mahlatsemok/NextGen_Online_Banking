package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.TransactionType;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.repository.BankAccountRepository;
import com.nextgen.onlinebanking.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BankAccountServiceConcurrencyTest {

    @Test
    void concurrentTransfersShouldNotCauseDoubleSpending()
            throws Exception {

        Long userId = 1L;

        BankAccount sourceAccount = new BankAccount();
        sourceAccount.setAccountNumber("1111111111");
        sourceAccount.setBalance(new BigDecimal("500.00"));

        BankAccount destinationAccount = new BankAccount();
        destinationAccount.setAccountNumber("2222222222");
        destinationAccount.setBalance(BigDecimal.ZERO);

        User user = new User();
        user.setId(userId);

        sourceAccount.setUser(user);

        BankAccountRepository repository = mock(BankAccountRepository.class);

        UserRepository userRepository = mock(UserRepository.class);

        TransactionService transactionService = mock(TransactionService.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(repository.findByAccountNumber("1111111111"))
                .thenReturn(Optional.of(sourceAccount));

        when(repository.findByAccountNumber("2222222222"))
                .thenReturn(Optional.of(destinationAccount));

        when(repository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(transactionService.findByIdempotencyKey(anyString()))
                .thenReturn(Optional.empty());

        BankAccountService service = new BankAccountService(
                repository,
                userRepository,
                transactionService);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successfulTransfers = new AtomicInteger(0);

        List<Throwable> exceptions = new ArrayList<>();

        Runnable transfer = () -> {

            try {

                startLatch.await();

                service.transfer(
                        userId,
                        "1111111111",
                        "2222222222",
                        new BigDecimal("400.00"),
                        null);

                successfulTransfers.incrementAndGet();

            } catch (Throwable exception) {

                synchronized (exceptions) {
                    exceptions.add(exception);
                }
            }
        };

        executor.submit(transfer);
        executor.submit(transfer);

        startLatch.countDown();

        executor.shutdown();

        assertTrue(
                executor.awaitTermination(
                        5,
                        TimeUnit.SECONDS));

        assertEquals(
                1,
                exceptions.size(),
                "Exactly one concurrent transfer should be rejected");

        assertTrue(
                exceptions.get(0) instanceof IllegalArgumentException,
                "Rejected transfer should throw IllegalArgumentException");

        assertEquals(
                "Insufficient funds",
                exceptions.get(0).getMessage());

        assertEquals(
                1,
                successfulTransfers.get(),
                "Exactly one concurrent transfer should succeed");

        assertEquals(
                new BigDecimal("100.00"),
                sourceAccount.getBalance());

        assertEquals(
                new BigDecimal("400.00"),
                destinationAccount.getBalance());

        verify(transactionService, times(1))
                .recordTransaction(
                        eq(TransactionType.TRANSFER),
                        eq(new BigDecimal("400.00")),
                        eq(sourceAccount),
                        eq(destinationAccount),
                        isNull());
    }
    
    @Test
void concurrentRequestsWithSameIdempotencyKeyShouldProcessOnlyOnce()
        throws Exception {

    Long userId = 1L;

    BankAccount sourceAccount = new BankAccount();
    sourceAccount.setAccountNumber("1111111111");
    sourceAccount.setBalance(new BigDecimal("1000.00"));

    BankAccount destinationAccount = new BankAccount();
    destinationAccount.setAccountNumber("2222222222");
    destinationAccount.setBalance(BigDecimal.ZERO);

    User user = new User();
    user.setId(userId);

    sourceAccount.setUser(user);

    BankAccountRepository repository = mock(BankAccountRepository.class);

    UserRepository userRepository = mock(UserRepository.class);

    TransactionService transactionService = mock(TransactionService.class);

    when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));

    when(repository.findByAccountNumber("1111111111"))
            .thenReturn(Optional.of(sourceAccount));

    when(repository.findByAccountNumber("2222222222"))
            .thenReturn(Optional.of(destinationAccount));

    when(repository.save(any(BankAccount.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    when(transactionService.findByIdempotencyKey("transfer-concurrent-001"))
            .thenReturn(Optional.empty());

    BankAccountService service = new BankAccountService(
            repository,
            userRepository,
            transactionService);

    ExecutorService executor = Executors.newFixedThreadPool(2);

    CountDownLatch startLatch = new CountDownLatch(1);

    AtomicInteger successfulTransfers = new AtomicInteger(0);

    List<Throwable> exceptions = new ArrayList<>();

    Runnable transfer = () -> {

        try {

            startLatch.await();

            service.transfer(
                    userId,
                    "1111111111",
                    "2222222222",
                    new BigDecimal("400.00"),
                    "transfer-concurrent-001");

            successfulTransfers.incrementAndGet();

        } catch (Throwable exception) {

            synchronized (exceptions) {
                exceptions.add(exception);
            }
        }
    };

    executor.submit(transfer);
    executor.submit(transfer);

    startLatch.countDown();

    executor.shutdown();

    assertTrue(
            executor.awaitTermination(
                    5,
                    TimeUnit.SECONDS));

    /*
     * Two identical requests used the same idempotency key.
     *
     * Exactly one transfer should actually move money.
     */
    assertEquals(
             2,
             successfulTransfers.get(),
            "Both identical idempotent requests should complete successfully");
             
    /*
     * Only R400 should have moved.
     */
    assertEquals(
            new BigDecimal("600.00"),
            sourceAccount.getBalance());

    assertEquals(
            new BigDecimal("400.00"),
            destinationAccount.getBalance());

    /*
     * Only one transaction should be recorded.
     */
    verify(transactionService, times(1))
            .recordTransaction(
                    eq(TransactionType.TRANSFER),
                    eq(new BigDecimal("400.00")),
                    eq(sourceAccount),
                    eq(destinationAccount),
                    eq("transfer-concurrent-001"));
}

}
