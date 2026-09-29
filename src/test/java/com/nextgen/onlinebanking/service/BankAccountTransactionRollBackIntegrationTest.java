package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.AccountStatus;
import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.Transaction;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.model.UserRole;
import com.nextgen.onlinebanking.repository.BankAccountRepository;
import com.nextgen.onlinebanking.repository.TransactionRepository;
import com.nextgen.onlinebanking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(BankAccountTransactionRollbackIntegrationTest.FailingTransactionServiceConfig.class)
class BankAccountTransactionRollbackIntegrationTest {

    private final BankAccountService bankAccountService;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    BankAccountTransactionRollbackIntegrationTest(
            BankAccountService bankAccountService,
            UserRepository userRepository,
            BankAccountRepository bankAccountRepository,
            TransactionRepository transactionRepository) {

        this.bankAccountService = bankAccountService;
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
        transactionRepository.flush();

        bankAccountRepository.deleteAll();
        bankAccountRepository.flush();

        userRepository.deleteAll();
        userRepository.flush();
    }

    @Test
    void shouldRollbackDepositWhenTransactionRecordingFails() {

        User user = new User(
                "Rollback",
                "Test",
                "rollback@example.com",
                "Password123");

        user.setRole(UserRole.USER);

        userRepository.saveAndFlush(user);

        String accountNumber = "9999999999";

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        bankAccountRepository.saveAndFlush(account);

        assertThrows(
                RuntimeException.class,
                () -> bankAccountService.deposit(
                        user.getId(),
                        accountNumber,
                        new BigDecimal("500.00"))
        );

        BankAccount reloadedAccount =
                bankAccountRepository
                        .findByAccountNumber(accountNumber)
                        .orElseThrow();

        assertEquals(
                0,
                reloadedAccount
                        .getBalance()
                        .compareTo(new BigDecimal("1000.00"))
        );

        assertEquals(
                0L,
                transactionRepository.count()
        );
    }

    @TestConfiguration
    static class FailingTransactionServiceConfig {

        @Bean
        @Primary
        TransactionService failingTransactionService() {

            return new TransactionService(null) {

                @Override
                public Transaction recordTransaction(
                        com.nextgen.onlinebanking.model.TransactionType type,
                        BigDecimal amount,
                        BankAccount sourceAccount,
                        BankAccount destinationAccount) {

                    throw new RuntimeException(
                            "Simulated transaction recording failure");
                }
            };
        }
    }

    @Test
    void shouldRollbackWithdrawalWhenTransactionRecordingFails() {

        User user = new User(
                "Withdrawal",
                "Rollback",
                "withdrawal.rollback@example.com",
                "Password123");

        user.setRole(UserRole.USER);

        userRepository.saveAndFlush(user);

        String accountNumber = "8888888888";

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        bankAccountRepository.saveAndFlush(account);

        assertThrows(
                RuntimeException.class,
                () -> bankAccountService.withdraw(
                        user.getId(),
                        accountNumber,
                        new BigDecimal("500.00")));

        BankAccount reloadedAccount = bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow();

        assertEquals(
                0,
                reloadedAccount
                        .getBalance()
                        .compareTo(new BigDecimal("1000.00")));

        assertEquals(
                0L,
                transactionRepository.count());
    }

    @Test
    void shouldRollbackTransferWhenTransactionRecordingFails() {

        User user = new User(
                "Transfer",
                "Rollback",
                "transfer.rollback@example.com",
                "Password123");

        user.setRole(UserRole.USER);

        userRepository.saveAndFlush(user);

        BankAccount sourceAccount = new BankAccount();
        sourceAccount.setAccountNumber("7777777777");
        sourceAccount.setUser(user);
        sourceAccount.setAccountType(AccountType.CHECKING);
        sourceAccount.setBalance(new BigDecimal("1000.00"));
        sourceAccount.setStatus(AccountStatus.ACTIVE);

        bankAccountRepository.saveAndFlush(sourceAccount);

        BankAccount destinationAccount = new BankAccount();
        destinationAccount.setAccountNumber("6666666666");
        destinationAccount.setUser(user);
        destinationAccount.setAccountType(AccountType.SAVINGS);
        destinationAccount.setBalance(new BigDecimal("200.00"));
        destinationAccount.setStatus(AccountStatus.ACTIVE);

        bankAccountRepository.saveAndFlush(destinationAccount);

        assertThrows(
                RuntimeException.class,
                () -> bankAccountService.transfer(
                        user.getId(),
                        "7777777777",
                        "6666666666",
                        new BigDecimal("500.00")));

        BankAccount reloadedSource = bankAccountRepository
                .findByAccountNumber("7777777777")
                .orElseThrow();

        BankAccount reloadedDestination = bankAccountRepository
                .findByAccountNumber("6666666666")
                .orElseThrow();

        assertEquals(
                0,
                reloadedSource
                        .getBalance()
                        .compareTo(new BigDecimal("1000.00")));

        assertEquals(
                0,
                reloadedDestination
                        .getBalance()
                        .compareTo(new BigDecimal("200.00")));

        assertEquals(
                0L,
                transactionRepository.count());
    }


}
