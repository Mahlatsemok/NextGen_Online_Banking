package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.service.BankAccountService;
import com.nextgen.onlinebanking.dto.CreateAccountRequest;
import com.nextgen.onlinebanking.dto.DepositRequest;
import com.nextgen.onlinebanking.dto.WithdrawRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(
            BankAccountService bankAccountService) {

        this.bankAccountService = bankAccountService;
    }

    @GetMapping
    public ResponseEntity<List<BankAccount>> getMyAccounts(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        List<BankAccount> accounts = bankAccountService.getAccountsForUser(user.getId());

        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping("/{accountNumber}")
    public ResponseEntity<BankAccount> getAccount(
            @PathVariable String accountNumber,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        BankAccount account = bankAccountService.getAccountForUser(
                user.getId(),
                accountNumber);

        return ResponseEntity.ok(account);
    }
    
    @PostMapping
    public ResponseEntity<BankAccount> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        BankAccount account = bankAccountService.createAccount(
                user.getId(),
                request.getAccountType());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(account);
    }
    
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<BankAccount> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        BankAccount account = bankAccountService.deposit(
                user.getId(),
                accountNumber,
                request.getAmount());

        return ResponseEntity.ok(account);
    }
    
    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<BankAccount> withdraw(
           @PathVariable String accountNumber,
           @Valid @RequestBody WithdrawRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        BankAccount account = bankAccountService.withdraw(
                user.getId(),
                accountNumber,
                request.getAmount());

        return ResponseEntity.ok(account);
    }
    
    @PostMapping("/{accountNumber}/freeze")
    public ResponseEntity<BankAccount> freezeAccount(
            @PathVariable String accountNumber,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        BankAccount account = bankAccountService.freezeAccount(
                user.getId(),
                accountNumber);

        return ResponseEntity.ok(account);
    }
    
    @PostMapping("/{accountNumber}/close")
public ResponseEntity<BankAccount> closeAccount(
        @PathVariable String accountNumber,
        Authentication authentication) {

    User user = (User) authentication.getPrincipal();

    BankAccount account = bankAccountService.closeAccount(
            user.getId(),
            accountNumber);

    return ResponseEntity.ok(account);
}




}
