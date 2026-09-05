package com.nextgen.onlinebanking.repository;

import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BankAccountRepositoryTest {

    @Autowired
    private BankAccountRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindBankAccountById() {

        User user = createUser("bankaccount-owner@example.com");

        BankAccount account = createAccount(
                "1234567890",
                user
        );

        BankAccount savedAccount = repository.save(account);

        Optional<BankAccount> found =
                repository.findById(savedAccount.getId());

        assertTrue(found.isPresent());
        assertEquals(
                "1234567890",
                found.get().getAccountNumber()
        );
        assertEquals(
                AccountType.CHECKING,
                found.get().getAccountType()
        );
        assertEquals(
                new BigDecimal("1000.00"),
                found.get().getBalance()
        );
    }

    @Test
    void shouldFindAccountByAccountNumber() {

        User user = createUser("accountnumber@example.com");

        repository.save(
                createAccount("9876543210", user)
        );

        Optional<BankAccount> found =
                repository.findByAccountNumber("9876543210");

        assertTrue(found.isPresent());

        assertEquals(
                "9876543210",
                found.get().getAccountNumber()
        );
    }

    @Test
    void shouldCheckIfAccountNumberExists() {

        User user = createUser("exists@example.com");

        repository.save(
                createAccount("1111111111", user)
        );

        assertTrue(
                repository.existsByAccountNumber("1111111111")
        );

        assertFalse(
                repository.existsByAccountNumber("2222222222")
        );
    }

    @Test
    void shouldFindAllAccountsBelongingToUser() {

        User user = createUser("accounts@example.com");

        repository.save(
                createAccount("1000000001", user)
        );

        repository.save(
                createAccount("1000000002", user)
        );

        List<BankAccount> accounts =
                repository.findByUserId(user.getId());

        assertEquals(2, accounts.size());

        assertTrue(
                accounts.stream()
                        .anyMatch(account ->
                                account.getAccountNumber()
                                        .equals("1000000001"))
        );

        assertTrue(
                accounts.stream()
                        .anyMatch(account ->
                                account.getAccountNumber()
                                        .equals("1000000002"))
        );
    }

    @Test
    void shouldReturnEmptyWhenAccountNumberDoesNotExist() {

        Optional<BankAccount> result =
                repository.findByAccountNumber("9999999999");

        assertTrue(result.isEmpty());
    }

    private User createUser(String email) {

        User user = new User();

        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPassword("password123");

        return userRepository.save(user);
    }

    private BankAccount createAccount(
        String accountNumber,
        User user
    ) {
        BankAccount account = new BankAccount();

        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("1000.00"));

        return account;
    }

}
