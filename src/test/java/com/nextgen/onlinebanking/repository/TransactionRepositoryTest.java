package com.nextgen.onlinebanking.repository;

import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.Transaction;
import com.nextgen.onlinebanking.model.TransactionType;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.model.UserRole;
import com.nextgen.onlinebanking.model.UserStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionRepositoryTest {

    @Test
    void shouldCreateTransaction() {

        User user = new User();
        user.setEmail("transaction@example.com");
        user.setPassword("password");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setAccountNumber("1000000001");
        account.setBalance(new BigDecimal("1000.00"));
        account.setAccountType(AccountType.CHECKING);

        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setSourceAccount(account);

        assertEquals(
                TransactionType.WITHDRAWAL,
                transaction.getType()
        );

        assertEquals(
                new BigDecimal("100.00"),
                transaction.getAmount()
        );

        assertEquals(
                account,
                transaction.getSourceAccount()
        );
    }
}
