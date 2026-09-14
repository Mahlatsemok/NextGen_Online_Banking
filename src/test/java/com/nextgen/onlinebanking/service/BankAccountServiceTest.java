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

}
