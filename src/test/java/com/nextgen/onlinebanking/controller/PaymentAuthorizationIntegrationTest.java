package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.model.AccountStatus;
import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.model.UserRole;
import com.nextgen.onlinebanking.repository.BankAccountRepository;
import com.nextgen.onlinebanking.repository.PaymentRepository;
import com.nextgen.onlinebanking.repository.TransactionRepository;
import com.nextgen.onlinebanking.repository.UserRepository;
import com.nextgen.onlinebanking.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {

        paymentRepository.deleteAll();
        paymentRepository.flush();

        transactionRepository.deleteAll();
        transactionRepository.flush();

        bankAccountRepository.deleteAll();
        bankAccountRepository.flush();

        userRepository.deleteAll();
        userRepository.flush();
    }

    @Test
    void shouldRejectPaymentWithoutToken()
            throws Exception {

        String requestBody = """
                {
                    "sourceAccountNumber": "1111111111",
                    "destinationAccountNumber": "2222222222",
                    "amount": 250.00,
                    "type": "STANDARD",
                    "description": "Test payment",
                    "idempotencyKey": "payment-api-001"
                }
                """;

        mockMvc.perform(
                post("/api/payments")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidPaymentRequest()
            throws Exception {

        User user = createUser(
                "john@example.com",
                "John",
                "User");

        String token = jwtService.generateToken(
                user.getEmail());

        String requestBody = """
                {
                    "sourceAccountNumber": "",
                    "destinationAccountNumber": "",
                    "amount": 0,
                    "type": null,
                    "description": "Invalid payment",
                    "idempotencyKey": ""
                }
                """;

        mockMvc.perform(
                post("/api/payments")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateStandardPaymentForAuthenticatedUser()
            throws Exception {

        User user = createUser(
                "john@example.com",
                "John",
                "User");

        createAccount(
                "1111111111",
                user,
                new BigDecimal("1000.00"));

        createAccount(
                "2222222222",
                user,
                new BigDecimal("500.00"));

        String token = jwtService.generateToken(
                user.getEmail());

        String requestBody = """
                {
                    "sourceAccountNumber": "1111111111",
                    "destinationAccountNumber": "2222222222",
                    "amount": 250.00,
                    "type": "STANDARD",
                    "description": "Test payment",
                    "idempotencyKey": "payment-api-001"
                }
                """;

        mockMvc.perform(
                post("/api/payments")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.amount")
                                .value(250.00))
                .andExpect(
                        jsonPath("$.status")
                                .value("COMPLETED"))
                .andExpect(
                        jsonPath("$.type")
                                .value("STANDARD"))
                .andExpect(
                        jsonPath("$.sourceAccountNumber")
                                .value("1111111111"))
                .andExpect(
                        jsonPath("$.destinationAccountNumber")
                                .value("2222222222"))
                .andExpect(
                        jsonPath("$.idempotencyKey")
                                .value("payment-api-001"));
    }

    @Test
    void shouldRejectPaymentFromAnotherUsersAccount()
            throws Exception {

        User owner = createUser(
                "owner@example.com",
                "John",
                "Owner");

        User attacker = createUser(
                "attacker@example.com",
                "Jane",
                "Attacker");

        createAccount(
                "1111111111",
                owner,
                new BigDecimal("1000.00"));

        createAccount(
                "2222222222",
                owner,
                new BigDecimal("500.00"));

        String token = jwtService.generateToken(
                attacker.getEmail());

        String requestBody = """
                {
                    "sourceAccountNumber": "1111111111",
                    "destinationAccountNumber": "2222222222",
                    "amount": 250.00,
                    "type": "STANDARD",
                    "description": "Unauthorized payment",
                    "idempotencyKey": "payment-api-unauthorized"
                }
                """;

        mockMvc.perform(
                post("/api/payments")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldProcessSamePaymentIdempotencyKeyOnlyOnce()
            throws Exception {

        User user = createUser(
                "john@example.com",
                "John",
                "User");

        createAccount(
                "1111111111",
                user,
                new BigDecimal("1000.00"));

        createAccount(
                "2222222222",
                user,
                new BigDecimal("500.00"));

        String token = jwtService.generateToken(
                user.getEmail());

        String requestBody = """
                {
                    "sourceAccountNumber": "1111111111",
                    "destinationAccountNumber": "2222222222",
                    "amount": 250.00,
                    "type": "STANDARD",
                    "description": "Idempotent payment",
                    "idempotencyKey": "payment-idempotency-001"
                }
                """;

        mockMvc.perform(
                post("/api/payments")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/payments")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());

        BankAccount sourceAccount =
                bankAccountRepository
                        .findByAccountNumber("1111111111")
                        .orElseThrow();

        BankAccount destinationAccount =
                bankAccountRepository
                        .findByAccountNumber("2222222222")
                        .orElseThrow();

        assertEquals(
                new BigDecimal("750.00"),
                sourceAccount.getBalance());

        assertEquals(
                new BigDecimal("750.00"),
                destinationAccount.getBalance());

        assertEquals(
                1,
                paymentRepository
                        .findByIdempotencyKey(
                                "payment-idempotency-001")
                        .stream()
                        .count());
    }

    private User createUser(
            String email,
            String firstName,
            String lastName) {

        User user = new User(
                firstName,
                lastName,
                email,
                passwordEncoder.encode("Password123"));

        user.setRole(UserRole.USER);

        return userRepository.save(user);
    }

    private BankAccount createAccount(
            String accountNumber,
            User user,
            BigDecimal balance) {

        BankAccount account = new BankAccount();

        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(balance);
        account.setStatus(AccountStatus.ACTIVE);

        return bankAccountRepository.save(account);
    }
}
