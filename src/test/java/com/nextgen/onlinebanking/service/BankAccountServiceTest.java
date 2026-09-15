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

@Test
void shouldReturnAccountStatus() {

        String accountNumber = "1234567890";

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setStatus(AccountStatus.ACTIVE);

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        AccountStatus result = bankAccountService.getAccountStatus(accountNumber);

        assertEquals(AccountStatus.ACTIVE, result);

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);
}

@Test
void shouldRejectStatusRequestWhenAccountDoesNotExist() {

        String accountNumber = "9999999999";

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService
                                        .getAccountStatus(accountNumber));

        assertEquals(
                        "Account not found",
                        exception.getMessage());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);
}

@Test
void shouldUpdateAccountStatus() {

        String accountNumber = "1234567890";

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setStatus(AccountStatus.ACTIVE);

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        when(bankAccountRepository.save(account))
                        .thenReturn(account);

        BankAccount result = bankAccountService.updateAccountStatus(
                        accountNumber,
                        AccountStatus.FROZEN);

        assertEquals(AccountStatus.FROZEN, result.getStatus());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository)
                        .save(account);
}

@Test
void shouldRejectNullAccountStatus() {

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.updateAccountStatus(
                                        "1234567890",
                                        null));

        assertEquals(
                        "Account status is required",
                        exception.getMessage());

        verifyNoInteractions(bankAccountRepository);
}

@Test
void shouldRejectStatusUpdateWhenAccountDoesNotExist() {

        String accountNumber = "9999999999";

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.updateAccountStatus(
                                        accountNumber,
                                        AccountStatus.FROZEN));

        assertEquals(
                        "Account not found",
                        exception.getMessage());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository, never())
                        .save(any(BankAccount.class));
}

@Test
void shouldFreezeActiveAccount() {

        String accountNumber = "1234567890";

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setStatus(AccountStatus.ACTIVE);

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        when(bankAccountRepository.save(account))
                        .thenReturn(account);

        BankAccount result = bankAccountService.freezeAccount(accountNumber);

        assertEquals(AccountStatus.FROZEN, result.getStatus());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository)
                        .save(account);
}

@Test
void shouldRejectFreezingClosedAccount() {

        String accountNumber = "1234567890";

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setStatus(AccountStatus.CLOSED);

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.freezeAccount(accountNumber));

        assertEquals(
                        "Closed account cannot be frozen",
                        exception.getMessage());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository, never())
                        .save(any(BankAccount.class));
}

@Test
void shouldRejectFreezingNonExistentAccount() {

        String accountNumber = "9999999999";

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.freezeAccount(accountNumber));

        assertEquals(
                        "Account not found",
                        exception.getMessage());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository, never())
                        .save(any(BankAccount.class));
}

@Test
void shouldCloseAccountWithZeroBalance() {

        String accountNumber = "1234567890";

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        when(bankAccountRepository.save(account))
                        .thenReturn(account);

        BankAccount result = bankAccountService.closeAccount(accountNumber);

        assertEquals(AccountStatus.CLOSED, result.getStatus());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository)
                        .save(account);
}

@Test
void shouldRejectClosingAccountWithNonZeroBalance() {

        String accountNumber = "1234567890";

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setBalance(new BigDecimal("100.00"));
        account.setStatus(AccountStatus.ACTIVE);

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.closeAccount(accountNumber));

        assertEquals(
                        "Account balance must be zero before closing",
                        exception.getMessage());

        assertEquals(
                        AccountStatus.ACTIVE,
                        account.getStatus());

        verify(bankAccountRepository, never())
                        .save(any(BankAccount.class));
}

@Test
void shouldRejectClosingAlreadyClosedAccount() {

        String accountNumber = "1234567890";

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.CLOSED);

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.closeAccount(accountNumber));

        assertEquals(
                        "Account is already closed",
                        exception.getMessage());

        verify(bankAccountRepository, never())
                        .save(any(BankAccount.class));
}

@Test
void shouldRejectClosingNonExistentAccount() {

        String accountNumber = "9999999999";

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.closeAccount(accountNumber));

        assertEquals(
                        "Account not found",
                        exception.getMessage());

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository, never())
                        .save(any(BankAccount.class));
}

@Test
void shouldReturnAccountWhenUserOwnsAccount() {

        Long userId = 1L;
        String accountNumber = "1234567890";

        User user = new User();
        user.setId(userId);

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(user);

        when(userRepository.findById(userId))
                        .thenReturn(Optional.of(user));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        BankAccount result = bankAccountService.getAccountForUser(
                        userId,
                        accountNumber);

        assertNotNull(result);
        assertEquals(accountNumber, result.getAccountNumber());
        assertEquals(userId, result.getUser().getId());

        verify(userRepository)
                        .findById(userId);

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);
}

@Test
void shouldRejectAccountWhenUserDoesNotOwnAccount() {

        Long requestingUserId = 1L;
        Long accountOwnerId = 2L;

        String accountNumber = "1234567890";

        User requestingUser = new User();
        requestingUser.setId(requestingUserId);

        User accountOwner = new User();
        accountOwner.setId(accountOwnerId);

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(accountOwner);

        when(userRepository.findById(requestingUserId))
                        .thenReturn(Optional.of(requestingUser));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.getAccountForUser(
                                        requestingUserId,
                                        accountNumber));

        assertEquals(
                        "Account does not belong to user",
                        exception.getMessage());

        verify(userRepository)
                        .findById(requestingUserId);

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);
}

@Test
void shouldRejectWhenAccountDoesNotExistForUser() {

        Long userId = 1L;
        String accountNumber = "9999999999";

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId))
                        .thenReturn(Optional.of(user));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.getAccountForUser(
                                        userId,
                                        accountNumber));

        assertEquals(
                        "Account not found",
                        exception.getMessage());

        verify(userRepository)
                        .findById(userId);

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);
}

@Test
void shouldDepositMoneyWhenUserOwnsAccount() {

        Long userId = 1L;
        String accountNumber = "1234567890";

        User user = new User();
        user.setId(userId);

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setBalance(new BigDecimal("100.00"));

        when(userRepository.findById(userId))
                        .thenReturn(Optional.of(user));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        when(bankAccountRepository.save(account))
                        .thenReturn(account);

        BankAccount result = bankAccountService.deposit(
                        userId,
                        accountNumber,
                        new BigDecimal("50.00"));

        assertEquals(
                        new BigDecimal("150.00"),
                        result.getBalance());

        verify(userRepository).findById(userId);
        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);
        verify(bankAccountRepository)
                        .save(account);
}

@Test
void shouldRejectDepositWhenUserDoesNotOwnAccount() {

        Long requestingUserId = 1L;
        Long ownerId = 2L;
        String accountNumber = "1234567890";

        User requestingUser = new User();
        requestingUser.setId(requestingUserId);

        User owner = new User();
        owner.setId(ownerId);

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(owner);
        account.setBalance(new BigDecimal("100.00"));

        when(userRepository.findById(requestingUserId))
                        .thenReturn(Optional.of(requestingUser));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.deposit(
                                        requestingUserId,
                                        accountNumber,
                                        new BigDecimal("50.00")));

        assertEquals(
                        "Account does not belong to user",
                        exception.getMessage());

        assertEquals(
                        new BigDecimal("100.00"),
                        account.getBalance());

        verify(bankAccountRepository, never())
                        .save(any(BankAccount.class));
}

@Test
void shouldWithdrawMoneyWhenUserOwnsAccount() {

        Long userId = 1L;
        String accountNumber = "1234567890";

        User user = new User();
        user.setId(userId);

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setBalance(new BigDecimal("200.00"));

        when(userRepository.findById(userId))
                        .thenReturn(Optional.of(user));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        when(bankAccountRepository.save(account))
                        .thenReturn(account);

        BankAccount result = bankAccountService.withdraw(
                        userId,
                        accountNumber,
                        new BigDecimal("50.00"));

        assertEquals(
                        new BigDecimal("150.00"),
                        result.getBalance());

        verify(userRepository).findById(userId);

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository)
                        .save(account);
}

@Test
void shouldRejectWithdrawalWhenUserDoesNotOwnAccount() {

        Long requestingUserId = 1L;
        Long ownerId = 2L;
        String accountNumber = "1234567890";

        User requestingUser = new User();
        requestingUser.setId(requestingUserId);

        User owner = new User();
        owner.setId(ownerId);

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(owner);
        account.setBalance(new BigDecimal("200.00"));

        when(userRepository.findById(requestingUserId))
                        .thenReturn(Optional.of(requestingUser));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.withdraw(
                                        requestingUserId,
                                        accountNumber,
                                        new BigDecimal("50.00")));

        assertEquals(
                        "Account does not belong to user",
                        exception.getMessage());

        // Balance must remain unchanged
        assertEquals(
                        new BigDecimal("200.00"),
                        account.getBalance());

        // Database must not be updated
        verify(bankAccountRepository, never())
                        .save(any(BankAccount.class));
}

@Test
void shouldFreezeAccountWhenUserOwnsAccount() {

        Long userId = 1L;
        String accountNumber = "1234567890";

        User user = new User();
        user.setId(userId);

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);

        when(userRepository.findById(userId))
                        .thenReturn(Optional.of(user));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        when(bankAccountRepository.save(account))
                        .thenReturn(account);

        BankAccount result = bankAccountService.freezeAccount(
                        userId,
                        accountNumber);

        assertEquals(
                        AccountStatus.FROZEN,
                        result.getStatus());

        verify(userRepository).findById(userId);

        verify(bankAccountRepository)
                        .findByAccountNumber(accountNumber);

        verify(bankAccountRepository)
                        .save(account);
}

@Test
void shouldRejectFreezingAccountWhenUserDoesNotOwnAccount() {

        Long requestingUserId = 1L;
        Long ownerId = 2L;
        String accountNumber = "1234567890";

        User requestingUser = new User();
        requestingUser.setId(requestingUserId);

        User owner = new User();
        owner.setId(ownerId);

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(owner);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);

        when(userRepository.findById(requestingUserId))
                        .thenReturn(Optional.of(requestingUser));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.freezeAccount(
                                        requestingUserId,
                                        accountNumber));

        assertEquals(
                        "Account does not belong to user",
                        exception.getMessage());

        // Account must remain unchanged
        assertEquals(
                        AccountStatus.ACTIVE,
                        account.getStatus());

        // No database update
        verify(bankAccountRepository, never())
                        .save(any(BankAccount.class));
}

@Test
void shouldCloseAccountWhenUserOwnsAccount() {

        Long userId = 1L;
        String accountNumber = "1234567890";

        User user = new User();
        user.setId(userId);

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);

        when(userRepository.findById(userId))
                        .thenReturn(Optional.of(user));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        when(bankAccountRepository.save(account))
                        .thenReturn(account);

        BankAccount result = bankAccountService.closeAccount(
                        userId,
                        accountNumber);

        assertEquals(
                        AccountStatus.CLOSED,
                        result.getStatus());

        verify(bankAccountRepository).save(account);
}

@Test
void shouldRejectClosingAccountWhenUserDoesNotOwnAccount() {

        Long requestingUserId = 1L;
        Long ownerId = 2L;
        String accountNumber = "1234567890";

        User requestingUser = new User();
        requestingUser.setId(requestingUserId);

        User owner = new User();
        owner.setId(ownerId);

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(owner);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);

        when(userRepository.findById(requestingUserId))
                        .thenReturn(Optional.of(requestingUser));

        when(bankAccountRepository.findByAccountNumber(accountNumber))
                        .thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(
                        IllegalArgumentException.class,
                        () -> bankAccountService.closeAccount(
                                        requestingUserId,
                                        accountNumber));

        assertEquals(
                        "Account does not belong to user",
                        exception.getMessage());

        assertEquals(
                        AccountStatus.ACTIVE,
                        account.getStatus());

        verify(bankAccountRepository, never())
                        .save(any(BankAccount.class));
}

}
