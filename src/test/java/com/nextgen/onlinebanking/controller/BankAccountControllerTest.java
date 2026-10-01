package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.service.BankAccountService;
import com.nextgen.onlinebanking.dto.CreateAccountRequest;
import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.dto.DepositRequest;
import com.nextgen.onlinebanking.dto.WithdrawRequest;
import com.nextgen.onlinebanking.model.AccountStatus;
import com.nextgen.onlinebanking.service.TransactionService;
import com.nextgen.onlinebanking.model.Transaction;
import com.nextgen.onlinebanking.model.TransactionType;
import com.nextgen.onlinebanking.dto.TransferRequest;

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

    private final TransactionService transactionService =
        mock(TransactionService.class);

    private final Authentication authentication =
        mock(Authentication.class);

    private final BankAccountController bankAccountController =
        new BankAccountController(
                bankAccountService,
                transactionService);


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

   @Test
   void shouldReturnTransactionHistoryForAuthenticatedUser() {

           Long userId = 1L;
           String accountNumber = "1234567890";

           User user = new User();
           user.setId(userId);

           BankAccount account = new BankAccount();
           account.setAccountNumber(accountNumber);
           account.setUser(user);

           Transaction deposit = new Transaction();
           deposit.setType(TransactionType.DEPOSIT);
           deposit.setAmount(new BigDecimal("500.00"));
           deposit.setDestinationAccount(account);

           Transaction withdrawal = new Transaction();
           withdrawal.setType(TransactionType.WITHDRAWAL);
           withdrawal.setAmount(new BigDecimal("100.00"));
           withdrawal.setSourceAccount(account);

           List<Transaction> transactions = Arrays.asList(deposit, withdrawal);

           when(authentication.getPrincipal())
                           .thenReturn(user);

           when(bankAccountService.getAccountForUser(
                           userId,
                           accountNumber))
                           .thenReturn(account);

           when(transactionService.getTransactionsForAccount(account))
                           .thenReturn(transactions);

           ResponseEntity<List<Transaction>> response = bankAccountController.getTransactions(
                           accountNumber,
                           authentication);

           assertEquals(200, response.getStatusCode().value());
           assertNotNull(response.getBody());

           assertEquals(2, response.getBody().size());

           assertEquals(
                           TransactionType.DEPOSIT,
                           response.getBody().get(0).getType());

           assertEquals(
                           new BigDecimal("500.00"),
                           response.getBody().get(0).getAmount());

           assertEquals(
                           TransactionType.WITHDRAWAL,
                           response.getBody().get(1).getType());

           assertEquals(
                           new BigDecimal("100.00"),
                           response.getBody().get(1).getAmount());

           verify(bankAccountService)
                           .getAccountForUser(
                                           userId,
                                           accountNumber);

           verify(transactionService)
                           .getTransactionsForAccount(account);
   }
   
   @Test
   void shouldTransferMoneyFromAuthenticatedUsersAccount() {

           Long userId = 1L;
           String sourceAccountNumber = "1234567890";
           String destinationAccountNumber = "9876543210";

           User user = new User();
           user.setId(userId);

           TransferRequest request = new TransferRequest();

           request.setDestinationAccountNumber(destinationAccountNumber);
           request.setAmount(new BigDecimal("500.00"));
           request.setIdempotencyKey("transfer-key-001");

           doNothing().when(bankAccountService).transfer(
                           userId,
                           sourceAccountNumber,
                           destinationAccountNumber,
                           new BigDecimal("500.00"),
                           "transfer-key-001");

           when(authentication.getPrincipal())
                           .thenReturn(user);

           ResponseEntity<Void> response = bankAccountController.transfer(
                           sourceAccountNumber,
                           request,
                           authentication);

           assertEquals(200, response.getStatusCode().value());
           assertNull(response.getBody());

           verify(bankAccountService).transfer(
                           userId,
                           sourceAccountNumber,
                           destinationAccountNumber,
                           new BigDecimal("500.00"),
                           "transfer-key-001");
   }

   @Test
   void shouldRejectInvalidDepositAmount() {

           Long userId = 1L;
           String accountNumber = "1234567890";

           User user = new User();
           user.setId(userId);

           DepositRequest request = new DepositRequest(new BigDecimal("0.00"));

           when(authentication.getPrincipal())
                           .thenReturn(user);

           when(bankAccountService.deposit(
                           userId,
                           accountNumber,
                           request.getAmount()))
                           .thenThrow(new IllegalArgumentException(
                                           "Deposit amount must be greater than zero"));

           IllegalArgumentException exception = assertThrows(
                           IllegalArgumentException.class,
                           () -> bankAccountController.deposit(
                                           accountNumber,
                                           request,
                                           authentication));

           assertEquals(
                           "Deposit amount must be greater than zero",
                           exception.getMessage());

           verify(bankAccountService)
                           .deposit(
                                           userId,
                                           accountNumber,
                                           new BigDecimal("0.00"));
   }

   @Test
   void shouldRejectInvalidWithdrawalAmount() {

           Long userId = 1L;
           String accountNumber = "1234567890";

           User user = new User();
           user.setId(userId);

           WithdrawRequest request = new WithdrawRequest(new BigDecimal("0.00"));

           when(authentication.getPrincipal())
                           .thenReturn(user);

           when(bankAccountService.withdraw(
                           userId,
                           accountNumber,
                           request.getAmount()))
                           .thenThrow(new IllegalArgumentException(
                                           "Withdrawal amount must be greater than zero"));

           IllegalArgumentException exception = assertThrows(
                           IllegalArgumentException.class,
                           () -> bankAccountController.withdraw(
                                           accountNumber,
                                           request,
                                           authentication));

           assertEquals(
                           "Withdrawal amount must be greater than zero",
                           exception.getMessage());

           verify(bankAccountService)
                           .withdraw(
                                           userId,
                                           accountNumber,
                                           new BigDecimal("0.00"));
   }

   @Test
   void shouldUseAuthenticatedUserIdWhenGettingAccount() {

           Long authenticatedUserId = 1L;
           String accountNumber = "1234567890";

           User user = new User();
           user.setId(authenticatedUserId);

           BankAccount account = new BankAccount();
           account.setAccountNumber(accountNumber);
           account.setUser(user);

           when(authentication.getPrincipal())
                           .thenReturn(user);

           when(bankAccountService.getAccountForUser(
                           authenticatedUserId,
                           accountNumber))
                           .thenReturn(account);

           ResponseEntity<BankAccount> response = bankAccountController.getAccount(
                           accountNumber,
                           authentication);

           assertEquals(200, response.getStatusCode().value());
           assertNotNull(response.getBody());

           assertEquals(
                           accountNumber,
                           response.getBody().getAccountNumber());

           verify(bankAccountService, times(1))
                           .getAccountForUser(
                                           eq(authenticatedUserId),
                                           eq(accountNumber));
   }


}
