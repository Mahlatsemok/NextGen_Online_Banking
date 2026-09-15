package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.service.BankAccountService;
import com.nextgen.onlinebanking.dto.CreateAccountRequest;
import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.dto.DepositRequest;
import com.nextgen.onlinebanking.dto.WithdrawRequest;
import com.nextgen.onlinebanking.model.AccountStatus;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import java.util.Arrays;
import java.util.List;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankAccountControllerTest {

    private final BankAccountService bankAccountService =
            mock(BankAccountService.class);

    private final Authentication authentication =
            mock(Authentication.class);

    private final BankAccountController bankAccountController =
            new BankAccountController(bankAccountService);

    @Test
    void shouldReturnAccountsForAuthenticatedUser() {

        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        BankAccount account1 = new BankAccount();
        account1.setUser(user);

        BankAccount account2 = new BankAccount();
        account2.setUser(user);

        List<BankAccount> accounts = Arrays.asList(account1, account2);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(bankAccountService.getAccountsForUser(userId))
                .thenReturn(accounts);

        var response = bankAccountController.getMyAccounts(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(bankAccountService)
                .getAccountsForUser(userId);
    }
    
    @Test
    void shouldReturnAccountForAuthenticatedUser() {

        Long userId = 1L;
        String accountNumber = "1234567890";

        User user = new User();
        user.setId(userId);

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(user);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(bankAccountService.getAccountForUser(
                userId,
                accountNumber))
                .thenReturn(account);

        var response = bankAccountController.getAccount(
                accountNumber,
                authentication);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                accountNumber,
                response.getBody().getAccountNumber());

        verify(bankAccountService)
                .getAccountForUser(userId, accountNumber);
    }

    @Test
    void shouldCreateAccountForAuthenticatedUser() {

        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        CreateAccountRequest request = new CreateAccountRequest();

        request.setAccountType(AccountType.CHECKING);

        BankAccount account = new BankAccount();
        account.setAccountNumber("1234567890");
        account.setUser(user);
        account.setAccountType(AccountType.CHECKING);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(bankAccountService.createAccount(
                userId,
                AccountType.CHECKING))
                .thenReturn(account);

        var response = bankAccountController.createAccount(
                request,
                authentication);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                "1234567890",
                response.getBody().getAccountNumber());

        assertEquals(
                AccountType.CHECKING,
                response.getBody().getAccountType());

        verify(bankAccountService)
                .createAccount(userId, AccountType.CHECKING);
    }

    @Test
    void shouldDepositMoneyIntoAuthenticatedUsersAccount() {

        Long userId = 1L;
        String accountNumber = "1234567890";

        User user = new User();
        user.setId(userId);

        DepositRequest request = new DepositRequest(new BigDecimal("500.00"));

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setBalance(new BigDecimal("1500.00"));

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(bankAccountService.deposit(
                userId,
                accountNumber,
                request.getAmount()))
                .thenReturn(account);

        var response = bankAccountController.deposit(
                accountNumber,
                request,
                authentication);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                new BigDecimal("1500.00"),
                response.getBody().getBalance());

        verify(bankAccountService)
                .deposit(
                        userId,
                        accountNumber,
                        new BigDecimal("500.00"));
    }

    @Test
    void shouldWithdrawMoneyFromAuthenticatedUsersAccount() {

        Long userId = 1L;
        String accountNumber = "1234567890";

        User user = new User();
        user.setId(userId);

        WithdrawRequest request = new WithdrawRequest(new BigDecimal("500.00"));

        BankAccount account = new BankAccount();
        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setBalance(new BigDecimal("500.00"));

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(bankAccountService.withdraw(
                userId,
                accountNumber,
                request.getAmount()))
                .thenReturn(account);

        var response = bankAccountController.withdraw(
                accountNumber,
                request,
                authentication);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                new BigDecimal("500.00"),
                response.getBody().getBalance());

        verify(bankAccountService)
                .withdraw(
                        userId,
                        accountNumber,
                        new BigDecimal("500.00"));
    }

   @Test
   void shouldFreezeAuthenticatedUsersAccount() {

       Long userId = 1L;
       String accountNumber = "1234567890";

       User user = new User();
       user.setId(userId);

       BankAccount account = new BankAccount();
       account.setAccountNumber(accountNumber);
       account.setUser(user);
       account.setBalance(new BigDecimal("1000.00"));
       account.setAccountType(AccountType.CHECKING);
       account.setStatus(AccountStatus.FROZEN);

       when(authentication.getPrincipal())
               .thenReturn(user);

       when(bankAccountService.freezeAccount(
               userId,
               accountNumber))
               .thenReturn(account);

       var response = bankAccountController.freezeAccount(
               accountNumber,
               authentication);

       assertEquals(200, response.getStatusCode().value());
       assertNotNull(response.getBody());

       assertEquals(
               accountNumber,
               response.getBody().getAccountNumber());

       assertEquals(
               AccountStatus.FROZEN,
               response.getBody().getStatus());

       verify(bankAccountService)
               .freezeAccount(userId, accountNumber);
   }

   @Test
   void shouldCloseAuthenticatedUsersAccount() {

       Long userId = 1L;
       String accountNumber = "1234567890";

       User user = new User();
       user.setId(userId);

       BankAccount account = new BankAccount();
       account.setAccountNumber(accountNumber);
       account.setUser(user);
       account.setBalance(BigDecimal.ZERO);
       account.setAccountType(AccountType.CHECKING);
       account.setStatus(AccountStatus.CLOSED);

       when(authentication.getPrincipal())
               .thenReturn(user);

       when(bankAccountService.closeAccount(
               userId,
               accountNumber))
               .thenReturn(account);

       var response = bankAccountController.closeAccount(
               accountNumber,
               authentication);

       assertEquals(200, response.getStatusCode().value());
       assertNotNull(response.getBody());

       assertEquals(
               accountNumber,
               response.getBody().getAccountNumber());

       assertEquals(
               AccountStatus.CLOSED,
               response.getBody().getStatus());

       assertEquals(
               BigDecimal.ZERO,
               response.getBody().getBalance());

       verify(bankAccountService)
               .closeAccount(userId, accountNumber);
   }





}
