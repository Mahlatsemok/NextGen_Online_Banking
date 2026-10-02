package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.Payment;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaymentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        transactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRejectQrPaymentWithoutToken() throws Exception {

        String request = """
                {
                    "sourceAccountNumber": "1000000001",
                    "qrCode": "NEXTGEN:QR:2000000001",
                    "amount": 100.00,
                    "description": "QR payment",
                    "idempotencyKey": "qr-001"
                }
                """;

        mockMvc.perform(
                        post("/api/payments/qr")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidQrPaymentRequest() throws Exception {

        User user = createUser("qr-invalid@test.com");
        createAccount(
                user,
                "1000000001",
                new BigDecimal("1000.00")
        );

        String token = jwtService.generateToken(user.getEmail());

        String request = """
                {
                    "sourceAccountNumber": "1000000001",
                    "qrCode": "INVALID-QR",
                    "amount": 100.00,
                    "description": "QR payment",
                    "idempotencyKey": "qr-invalid-001"
                }
                """;

        mockMvc.perform(
                        post("/api/payments/qr")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateQrPaymentForAuthenticatedUser() throws Exception {

        User user = createUser("qr-payment@test.com");

        BankAccount source = createAccount(
                user,
                "1000000001",
                new BigDecimal("1000.00")
        );

        User destinationUser = createUser("qr-destination@test.com");

        createAccount(
                destinationUser,
                "2000000001",
                new BigDecimal("500.00")
        );

        String token = jwtService.generateToken(user.getEmail());

        String request = """
                {
                    "sourceAccountNumber": "1000000001",
                    "qrCode": "NEXTGEN:QR:2000000001",
                    "amount": 100.00,
                    "description": "QR payment",
                    "idempotencyKey": "qr-payment-001"
                }
                """;

        mockMvc.perform(
                        post("/api/payments/qr")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated());

        BankAccount updatedSource =
                bankAccountRepository
                        .findById(source.getId())
                        .orElseThrow();

        assertEquals(
                new BigDecimal("900.00"),
                updatedSource.getBalance()
        );

        Payment payment =
                paymentRepository
                        .findByIdempotencyKey("qr-payment-001")
                        .orElseThrow();

        assertEquals(
                "1000000001",
                payment.getSourceAccount().getAccountNumber()
        );

        assertEquals(
                "2000000001",
                payment.getDestinationAccount().getAccountNumber()
        );
    }

    @Test
    void shouldRejectQrPaymentFromAnotherUsersAccount() throws Exception {

        User owner = createUser("qr-owner@test.com");

        createAccount(
                owner,
                "1000000001",
                new BigDecimal("1000.00")
        );

        User attacker = createUser("qr-attacker@test.com");

        createAccount(
                attacker,
                "3000000001",
                new BigDecimal("1000.00")
        );

        createAccount(
                owner,
                "2000000001",
                new BigDecimal("500.00")
        );

        String token = jwtService.generateToken(attacker.getEmail());

        String request = """
                {
                    "sourceAccountNumber": "1000000001",
                    "qrCode": "NEXTGEN:QR:2000000001",
                    "amount": 100.00,
                    "description": "Unauthorized QR payment",
                    "idempotencyKey": "qr-unauthorized-001"
                }
                """;

        mockMvc.perform(
                        post("/api/payments/qr")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectScheduledPaymentWithoutToken() throws Exception {

        String futureDate =
                LocalDateTime.now()
                        .plusDays(1)
                        .withNano(0)
                        .toString();

        String request = """
                {
                    "sourceAccountNumber": "1000000001",
                    "destinationAccountNumber": "2000000001",
                    "amount": 100.00,
                    "scheduledAt": "%s",
                    "description": "Scheduled payment",
                    "idempotencyKey": "scheduled-001"
                }
                """.formatted(futureDate);

        mockMvc.perform(
                        post("/api/payments/scheduled")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateScheduledPaymentForAuthenticatedUser()
            throws Exception {

        User user = createUser("scheduled@test.com");

        createAccount(
                user,
                "1000000001",
                new BigDecimal("1000.00")
        );

        User destinationUser =
                createUser("scheduled-destination@test.com");

        createAccount(
                destinationUser,
                "2000000001",
                new BigDecimal("500.00")
        );

        String token = jwtService.generateToken(user.getEmail());

        String futureDate =
                LocalDateTime.now()
                        .plusDays(1)
                        .withNano(0)
                        .toString();

        String request = """
                {
                    "sourceAccountNumber": "1000000001",
                    "destinationAccountNumber": "2000000001",
                    "amount": 100.00,
                    "scheduledAt": "%s",
                    "description": "Scheduled payment",
                    "idempotencyKey": "scheduled-001"
                }
                """.formatted(futureDate);

        mockMvc.perform(
                        post("/api/payments/scheduled")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated());

        Payment payment =
                paymentRepository
                        .findByIdempotencyKey("scheduled-001")
                        .orElseThrow();

        assertEquals(
                new BigDecimal("100.00"),
                payment.getAmount()
        );

        assertEquals(
                "SCHEDULED",
                payment.getStatus().name()
        );

        assertEquals(
                "SCHEDULED",
                payment.getType().name()
        );
    }

    @Test
    void shouldRejectPastScheduledPayment() throws Exception {

        User user = createUser("scheduled-past@test.com");

        createAccount(
                user,
                "1000000001",
                new BigDecimal("1000.00")
        );

        User destinationUser =
                createUser("scheduled-past-destination@test.com");

        createAccount(
                destinationUser,
                "2000000001",
                new BigDecimal("500.00")
        );

        String token = jwtService.generateToken(user.getEmail());

        String pastDate =
                LocalDateTime.now()
                        .minusDays(1)
                        .withNano(0)
                        .toString();

        String request = """
                {
                    "sourceAccountNumber": "1000000001",
                    "destinationAccountNumber": "2000000001",
                    "amount": 100.00,
                    "scheduledAt": "%s",
                    "description": "Invalid scheduled payment",
                    "idempotencyKey": "scheduled-past-001"
                }
                """.formatted(pastDate);

        mockMvc.perform(
                        post("/api/payments/scheduled")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    private User createUser(String email) {

        User user = new User();

         user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setRole(UserRole.USER);

        return userRepository.save(user);
    }

    private BankAccount createAccount(
            User user,
            String accountNumber,
            BigDecimal balance) {

        BankAccount account = new BankAccount();

        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(balance);

        return bankAccountRepository.save(account);
    }
}
