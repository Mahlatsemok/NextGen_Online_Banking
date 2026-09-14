package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.AccountStatus;
import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.repository.BankAccountRepository;
import com.nextgen.onlinebanking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.Arrays;
import java.util.List;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BankAccountServiceTest {

    private final BankAccountRepository bankAccountRepository =
            mock(BankAccountRepository.class);

    private final UserRepository userRepository =
            mock(UserRepository.class);

    private final BankAccountService bankAccountService =
            new BankAccountService(
                    bankAccountRepository,
                    userRepository
            );

    @Test
    void shouldCreateBankAccountForExistingUser() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount result =
                bankAccountService.createAccount(
                        1L,
                        AccountType.SAVINGS
                );

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(AccountType.SAVINGS, result.getAccountType());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals(0, result.getBalance().compareTo(java.math.BigDecimal.ZERO));
        assertNotNull(result.getAccountNumber());

        verify(userRepository).findById(1L);
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldRejectAccountCreationWhenUserDoesNotExist() {

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> bankAccountService.createAccount(
                        999L,
                        AccountType.CHECKING
                )
        );

        verify(userRepository).findById(999L);
        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));
    }

    @Test
    void shouldRejectNullAccountType() {

        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> bankAccountService.createAccount(
                        1L,
                        null
                )
        );

        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));
    }

    @Test
    void shouldGenerateUniqueAccountNumber() {

            User user = new User();
            user.setId(1L);

            when(userRepository.findById(1L))
                            .thenReturn(Optional.of(user));

            when(bankAccountRepository.existsByAccountNumber(anyString()))
                            .thenReturn(false);

            when(bankAccountRepository.save(any(BankAccount.class)))
                            .thenAnswer(invocation -> invocation.getArgument(0));

            BankAccount result = bankAccountService.createAccount(
                            1L,
                            AccountType.CHECKING);

            assertNotNull(result.getAccountNumber());
            assertFalse(result.getAccountNumber().isBlank());

            verify(bankAccountRepository)
                            .existsByAccountNumber(anyString());

            verify(bankAccountRepository)
                            .save(any(BankAccount.class));
    }
    
    @Test
void shouldReturnAllAccountsForUser() {

    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");

    BankAccount checking = new BankAccount();
    checking.setAccountNumber("1000000001");
    checking.setUser(user);
    checking.setAccountType(AccountType.CHECKING);

    BankAccount savings = new BankAccount();
    savings.setAccountNumber("1000000002");
    savings.setUser(user);
    savings.setAccountType(AccountType.SAVINGS);

    when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

    when(bankAccountRepository.findByUserId(1L))
            .thenReturn(Arrays.asList(checking, savings));

    List<BankAccount> result =
            bankAccountService.getAccountsForUser(1L);

    assertNotNull(result);
    assertEquals(2, result.size());

    assertTrue(result.contains(checking));
    assertTrue(result.contains(savings));

    verify(userRepository).findById(1L);
    verify(bankAccountRepository).findByUserId(1L);
}

@Test
void shouldRejectWhenUserDoesNotExist() {

        when(userRepository.findById(999L))
                        .thenReturn(Optional.empty());

        assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.getAccountsForUser(999L));

        verify(userRepository).findById(999L);

        verify(bankAccountRepository, never())
                        .findByUserId(anyLong());
}

@Test
void shouldReturnAccountByAccountNumber() {

        String accountNumber = "1234567890";

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        BankAccount result = bankAccountService.getAccountByAccountNumber(accountNumber);

        assertNotNull(result);
        assertEquals(accountNumber, result.getAccountNumber());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);
}

@Test
void shouldRejectWhenAccountNumberDoesNotExist() {

        String accountNumber = "9999999999";

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService
                                        .getAccountByAccountNumber(accountNumber));

        assertEquals("Account not found", exception.getMessage());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);
}

@Test
void shouldDepositMoneyIntoAccount() {
        String accountNumber = "1234567890";

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setBalance(new BigDecimal("100.00"));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        when(bankAccountRepository.save(account))
                        .thenReturn(account);

        BankAccount result = bankAccountService.deposit(
                        accountNumber,
                        new BigDecimal("50.00"));

        assertEquals(
                        new BigDecimal("150.00"),
                        result.getBalance());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository)
                        .save(account);
}


@Test
void shouldRejectNegativeDeposit() {

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.deposit(
                                        "1234567890",
                                        new BigDecimal("-50.00")));

        assertEquals(
                        "Deposit amount must be greater than zero",
                        exception.getMessage());

        verifyNoInteractions(bankAccountRepository);
}

@Test
void shouldRejectZeroDeposit() {

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.deposit(
                                        "1234567890",
                                        BigDecimal.ZERO));

        assertEquals(
                        "Deposit amount must be greater than zero",
                        exception.getMessage());

        verifyNoInteractions(bankAccountRepository);
}

@Test
void shouldWithdrawMoneyFromAccount() {

        String accountNumber = "1234567890";

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setBalance(new BigDecimal("100.00"));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        when(bankAccountRepository.save(account))
                        .thenReturn(account);

        BankAccount result = bankAccountService.withdraw(
                        accountNumber,
                        new BigDecimal("40.00"));

        assertEquals(
                        new BigDecimal("60.00"),
                        result.getBalance());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository)
                        .save(account);
}

@Test
void shouldRejectWithdrawalWhenInsufficientFunds() {

        String accountNumber = "1234567890";

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setBalance(new BigDecimal("50.00"));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.withdraw(
                                        accountNumber,
                                        new BigDecimal("100.00")));

        assertEquals(
                        "Insufficient funds",
                        exception.getMessage());

        assertEquals(
                        new BigDecimal("50.00"),
                        account.getBalance());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository, never())
                        .save(any(BankAccount.class));
}

@Test
void shouldRejectNegativeWithdrawal() {

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.withdraw(
                                        "1234567890",
                                        new BigDecimal("-20.00")));

        assertEquals(
                        "Withdrawal amount must be greater than zero",
                        exception.getMessage());

        verifyNoInteractions(bankAccountRepository);
}

@Test
void shouldRejectZeroWithdrawal() {

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.withdraw(
                                        "1234567890",
                                        BigDecimal.ZERO));

        assertEquals(
                        "Withdrawal amount must be greater than zero",
                        exception.getMessage());

        verifyNoInteractions(bankAccountRepository);
}

@Test
void shouldRejectDepositWhenAccountDoesNotExist() {

        String accountNumber = "9999999999";

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.deposit(
                                        accountNumber,
                                        new BigDecimal("50.00")));

        assertEquals(
                        "Account not found",
                        exception.getMessage());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository, never())
                        .save(any(BankAccount.class));
}

@Test
void shouldRejectWithdrawalWhenAccountDoesNotExist() {

        String accountNumber = "9999999999";

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.withdraw(
                                        accountNumber,
                                        new BigDecimal("50.00")));

        assertEquals(
                        "Account not found",
                        exception.getMessage());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository, never())
                        .save(any(BankAccount.class));
}

}
