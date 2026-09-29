package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.Transaction;
import com.nextgen.onlinebanking.model.TransactionType;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private BankAccount account;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        User user = new User();
        user.setId(1L);
        user.setEmail("transaction@example.com");
        user.setPassword("password");

        account = new BankAccount();
        //account.setId(1L);
        account.setAccountNumber("1000000001");
        account.setUser(user);
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("1000.00"));
    }

    @Test
    void shouldRecordTransaction() {

        Transaction transaction = new Transaction();

        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setSourceAccount(account);

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        Transaction result = transactionService.recordTransaction(
                TransactionType.WITHDRAWAL,
                new BigDecimal("100.00"),
                account,
                null
        );

        assertNotNull(result);
        assertEquals(
                TransactionType.WITHDRAWAL,
                result.getType()
        );
        assertEquals(
                new BigDecimal("100.00"),
                result.getAmount()
        );
        assertEquals(
                account,
                result.getSourceAccount()
        );

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldRecordDepositTransaction() {

        Transaction transaction = new Transaction();

        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setDestinationAccount(account);

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        Transaction result = transactionService.recordTransaction(
                TransactionType.DEPOSIT,
                new BigDecimal("500.00"),
                null,
                account
        );

        assertNotNull(result);
        assertEquals(
                TransactionType.DEPOSIT,
                result.getType()
        );
        assertEquals(
                new BigDecimal("500.00"),
                result.getAmount()
        );
        assertEquals(
                account,
                result.getDestinationAccount()
        );

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldRejectNullTransactionType() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transactionService.recordTransaction(
                                null,
                                new BigDecimal("100.00"),
                                account,
                                null
                        )
                );

        assertEquals(
                "Transaction type is required",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldRejectNullAmount() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transactionService.recordTransaction(
                                TransactionType.DEPOSIT,
                                null,
                                null,
                                account
                        )
                );

        assertEquals(
                "Transaction amount must be greater than zero",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldRejectZeroAmount() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transactionService.recordTransaction(
                                TransactionType.DEPOSIT,
                                BigDecimal.ZERO,
                                null,
                                account
                        )
                );

        assertEquals(
                "Transaction amount must be greater than zero",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldRejectNegativeAmount() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transactionService.recordTransaction(
                                TransactionType.WITHDRAWAL,
                                new BigDecimal("-100.00"),
                                account,
                                null
                        )
                );

        assertEquals(
                "Transaction amount must be greater than zero",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldRecordTransferTransaction() {

        BankAccount destinationAccount = new BankAccount();
        //destinationAccount.setId(2L);
        destinationAccount.setAccountNumber("1000000002");

        Transaction transaction = new Transaction();

        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(new BigDecimal("250.00"));
        transaction.setSourceAccount(account);
        transaction.setDestinationAccount(destinationAccount);

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        Transaction result = transactionService.recordTransaction(
                TransactionType.TRANSFER,
                new BigDecimal("250.00"),
                account,
                destinationAccount);

        assertNotNull(result);
        assertEquals(
                TransactionType.TRANSFER,
                result.getType());
        assertEquals(
                new BigDecimal("250.00"),
                result.getAmount());
        assertEquals(
                account,
                result.getSourceAccount());
        assertEquals(
                destinationAccount,
                result.getDestinationAccount());

        verify(transactionRepository).save(any(Transaction.class));
    }
    
    @Test
    void shouldGetTransactionsForAccount() {

        BankAccount account = mock(BankAccount.class);

        when(account.getId())
                .thenReturn(1L);

        Transaction deposit = new Transaction();
        deposit.setType(TransactionType.DEPOSIT);
        deposit.setAmount(new BigDecimal("500.00"));
        deposit.setDestinationAccount(account);

        Transaction withdrawal = new Transaction();
        withdrawal.setType(TransactionType.WITHDRAWAL);
        withdrawal.setAmount(new BigDecimal("100.00"));
        withdrawal.setSourceAccount(account);

        when(transactionRepository
                .findBySourceAccountIdOrDestinationAccountId(
                        1L,
                        1L))
                .thenReturn(List.of(deposit, withdrawal));

        List<Transaction> transactions = transactionService.getTransactionsForAccount(account);

        assertEquals(2, transactions.size());

        assertEquals(
                TransactionType.DEPOSIT,
                transactions.get(0).getType());

        assertEquals(
                TransactionType.WITHDRAWAL,
                transactions.get(1).getType());

        verify(transactionRepository)
                .findBySourceAccountIdOrDestinationAccountId(
                        1L,
                        1L);
    }


    @Test
    void shouldRejectNullAccountWhenGettingTransactions() {

            assertThrows(
                            IllegalArgumentException.class,
                            () -> transactionService
                                            .getTransactionsForAccount(null));

            verifyNoInteractions(transactionRepository);
    }
    
    @Test
void shouldFindTransactionByIdempotencyKey() {

    Transaction transaction = new Transaction();
    transaction.setType(TransactionType.TRANSFER);
    transaction.setAmount(new BigDecimal("250.00"));
    transaction.setIdempotencyKey("transfer-123");

    when(transactionRepository.findByIdempotencyKey("transfer-123"))
            .thenReturn(java.util.Optional.of(transaction));

    var result = transactionService.findByIdempotencyKey("transfer-123");

    assertTrue(result.isPresent());
    assertEquals(
            "transfer-123",
            result.get().getIdempotencyKey()
    );

    verify(transactionRepository)
            .findByIdempotencyKey("transfer-123");
}

@Test
void shouldReturnEmptyForBlankIdempotencyKey() {

        var result = transactionService.findByIdempotencyKey(" ");

        assertTrue(result.isEmpty());

        verify(transactionRepository, never())
                        .findByIdempotencyKey(anyString());
}



}
