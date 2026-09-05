package com.nextgen.onlinebanking.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BankAccountTest {

    @Test
    void shouldCreateBankAccountWithDefaultValues() {

        BankAccount account = new BankAccount();

        assertNull(account.getId());
        assertNull(account.getAccountNumber());
        assertNull(account.getUser());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertNull(account.getAccountType());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertNull(account.getCreatedAt());
        assertNull(account.getUpdatedAt());
    }

    @Test
    void shouldSetAndGetAccountNumber() {

        BankAccount account = new BankAccount();

        account.setAccountNumber("1234567890");

        assertEquals("1234567890", account.getAccountNumber());
    }

    @Test
    void shouldSetAndGetUser() {

        BankAccount account = new BankAccount();
        User user = new User();

        account.setUser(user);

        assertSame(user, account.getUser());
    }

    @Test
    void shouldSetAndGetBalance() {

        BankAccount account = new BankAccount();

        BigDecimal balance = new BigDecimal("1500.50");

        account.setBalance(balance);

        assertEquals(balance, account.getBalance());
    }

    @Test
    void shouldSetAndGetAccountType() {

        BankAccount account = new BankAccount();

        account.setAccountType(AccountType.SAVINGS);

        assertEquals(AccountType.SAVINGS, account.getAccountType());
    }

    @Test
    void shouldSetAndGetStatus() {

        BankAccount account = new BankAccount();

        account.setStatus(AccountStatus.FROZEN);

        assertEquals(AccountStatus.FROZEN, account.getStatus());
    }

    @Test
    void shouldSetCreationAndUpdateTimestampsWhenCreated() {

        BankAccount account = new BankAccount();

        account.onCreate();

        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());

        assertEquals(
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    @Test
    void shouldUpdateUpdatedAtWhenUpdated() throws InterruptedException {

        BankAccount account = new BankAccount();

        account.onCreate();

        LocalDateTime originalUpdatedAt = account.getUpdatedAt();

        Thread.sleep(1);

        account.onUpdate();

        assertNotNull(account.getUpdatedAt());
        assertTrue(
                account.getUpdatedAt().isAfter(originalUpdatedAt)
        );
    }

    @Test
    void shouldSupportAllAccountTypes() {

        assertEquals(2, AccountType.values().length);

        assertTrue(java.util.Arrays.asList(AccountType.values())
                .contains(AccountType.CHECKING));

        assertTrue(java.util.Arrays.asList(AccountType.values())
                .contains(AccountType.SAVINGS));
    }

    @Test
    void shouldSupportAllAccountStatuses() {

        assertEquals(3, AccountStatus.values().length);

        assertTrue(java.util.Arrays.asList(AccountStatus.values())
                .contains(AccountStatus.ACTIVE));

        assertTrue(java.util.Arrays.asList(AccountStatus.values())
                .contains(AccountStatus.FROZEN));

        assertTrue(java.util.Arrays.asList(AccountStatus.values())
                .contains(AccountStatus.CLOSED));
    }
}
