package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.model.AccountStatus;
import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.PaymentRequest;
import com.nextgen.onlinebanking.model.PaymentRequestStatus;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.model.UserRole;
import com.nextgen.onlinebanking.repository.BankAccountRepository;
import com.nextgen.onlinebanking.repository.PaymentRequestRepository;
import com.nextgen.onlinebanking.repository.UserRepository;
import com.nextgen.onlinebanking.security.JwtService;
import com.nextgen.onlinebanking.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentRequestAuthorizationIntegrationTest {

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
    private PaymentRequestRepository paymentRequestRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {

        paymentRequestRepository.deleteAll();
        paymentRequestRepository.flush();

         transactionRepository.deleteAll();
         transactionRepository.flush();

        bankAccountRepository.deleteAll();
        bankAccountRepository.flush();

        userRepository.deleteAll();
        userRepository.flush();
    }

    @Test
    void shouldRejectCreateRequestWithoutToken()
            throws Exception {

        String requestBody = """
                {
                    "requesterAccountNumber": "1111111111",
                    "requestedFromAccountNumber": "2222222222",
                    "amount": 250.00,
                    "description": "Payment request"
                }
                """;

        mockMvc.perform(
                post("/api/payment-requests")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidCreateRequest()
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
                new BigDecimal("1000.00"));

        String token = jwtService.generateToken(user.getEmail());

        String requestBody = """
                {
                    "requesterAccountNumber": "1111111111",
                    "requestedFromAccountNumber": "2222222222",
                    "amount": 0,
                    "description": "Invalid request"
                }
                """;

        mockMvc.perform(
                post("/api/payment-requests")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreatePaymentRequestForAuthenticatedUser()
            throws Exception {

        User requester = createUser(
                "requester@example.com",
                "John",
                "Requester");

        User requestedFrom = createUser(
                "payer@example.com",
                "Jane",
                "Payer");

        createAccount(
                "1111111111",
                requester,
                new BigDecimal("1000.00"));

        createAccount(
                "2222222222",
                requestedFrom,
                new BigDecimal("1000.00"));

        String token =
                jwtService.generateToken(requester.getEmail());

        String requestBody = """
                {
                    "requesterAccountNumber": "1111111111",
                    "requestedFromAccountNumber": "2222222222",
                    "amount": 250.00,
                    "description": "Payment request"
                }
                """;

        mockMvc.perform(
                post("/api/payment-requests")
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
                                .value("PENDING"));
    }

    @Test
    void shouldRejectUserFromAccessingAnotherUsersPaymentRequest()
            throws Exception {

        User owner = createUser(
                "owner@example.com",
                "John",
                "Owner");

        User requestedFromUser = createUser(
                "payer@example.com",
                "Jane",
                "Payer");

        User unrelatedUser = createUser(
                "unrelated@example.com",
                "Bob",
                "Unrelated");

        BankAccount ownerAccount = createAccount(
                "1111111111",
                owner,
                new BigDecimal("1000.00"));

        BankAccount requestedFromAccount = createAccount(
                "2222222222",
                requestedFromUser,
                new BigDecimal("1000.00"));

        PaymentRequest paymentRequest = new PaymentRequest();

        paymentRequest.setRequesterAccount(ownerAccount);
        paymentRequest.setRequestedFromAccount(requestedFromAccount);
        paymentRequest.setAmount(new BigDecimal("250.00"));
        paymentRequest.setDescription("Payment request");
        paymentRequest.setStatus(PaymentRequestStatus.PENDING);

        PaymentRequest savedRequest = paymentRequestRepository.save(paymentRequest);

        String token = jwtService.generateToken(
                unrelatedUser.getEmail());

        mockMvc.perform(
                get("/api/payment-requests/"
                        + savedRequest.getRequestReference())
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectUnauthenticatedGetRequest()
            throws Exception {

        mockMvc.perform(
                get("/api/payment-requests/REQ-DOES-NOT-EXIST"))
                .andExpect(status().isUnauthorized());
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

    private User createUserTokenUser(User user) {
        return user;
    }
}
