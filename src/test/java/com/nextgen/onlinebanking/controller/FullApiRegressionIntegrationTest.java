package com.nextgen.onlinebanking.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.repository.BankAccountRepository;
import com.nextgen.onlinebanking.repository.TransactionRepository;
import com.nextgen.onlinebanking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

@SpringBootTest
@AutoConfigureMockMvc
class FullApiRegressionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {

        transactionRepository.deleteAll();
        transactionRepository.flush();

        bankAccountRepository.deleteAll();
        bankAccountRepository.flush();

        userRepository.deleteAll();
        userRepository.flush();
    }

    @Test
    void shouldCompleteFullCustomerBankingJourney() throws Exception {

        /*
         * 1. REGISTER
         */
        String registerRequest = """
                {
                    "firstName": "Regression",
                    "lastName": "Tester",
                    "email": "regression@test.com",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("regression@test.com"))
                .andExpect(jsonPath("$.firstName")
                        .value("Regression"))
                .andExpect(jsonPath("$.lastName")
                        .value("Tester"));

        /*
         * 2. LOGIN
         */
        String loginRequest = """
                {
                    "email": "regression@test.com",
                    "password": "Password123!"
                }
                """;

        String loginResponse = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email")
                        .value("regression@test.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson =
                objectMapper.readTree(loginResponse);

        String token =
                loginJson.get("token").asText();

        /*
         * 3. ACCESS PROFILE WITH JWT
         */
        mockMvc.perform(
                get("/api/auth/profile")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value("regression@test.com"));

        /*
         * 4. CREATE FIRST ACCOUNT
         */
        String createAccountRequest = """
                {
                    "accountType": "CHECKING"
                }
                """;

        String accountResponse = mockMvc.perform(
                post("/api/accounts")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createAccountRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber")
                        .exists())
                .andExpect(jsonPath("$.balance")
                        .value(0))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode accountJson =
                objectMapper.readTree(accountResponse);

        String sourceAccountNumber =
                accountJson.get("accountNumber").asText();

        /*
         * 5. DEPOSIT MONEY
         */
        String depositRequest = """
                {
                    "amount": 1000.00
                }
                """;

        mockMvc.perform(
                post("/api/accounts/"
                        + sourceAccountNumber
                        + "/deposit")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance")
                        .value(1000.00));

        /*
         * 6. CREATE SECOND ACCOUNT
         */
        String secondAccountResponse = mockMvc.perform(
                post("/api/accounts")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createAccountRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber")
                        .exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode secondAccountJson =
                objectMapper.readTree(secondAccountResponse);

        String destinationAccountNumber =
                secondAccountJson
                        .get("accountNumber")
                        .asText();

        /*
         * 7. TRANSFER BETWEEN OWN ACCOUNTS
         */
        String transferRequest = """
                {
                    "destinationAccountNumber": "%s",
                    "amount": 250.00,
                    "idempotencyKey": "regression-transfer-001"
                }
                """.formatted(destinationAccountNumber);

        mockMvc.perform(
                post("/api/accounts/"
                        + sourceAccountNumber
                        + "/transfer")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferRequest))
                .andExpect(status().isOk());

        /*
         * 8. VERIFY SOURCE ACCOUNT BALANCE
         */
        mockMvc.perform(
                get("/api/accounts/"
                        + sourceAccountNumber)
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance")
                        .value(750.00));

        /*
         * 9. VERIFY DESTINATION ACCOUNT BALANCE
         */
        mockMvc.perform(
                get("/api/accounts/"
                        + destinationAccountNumber)
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance")
                        .value(250.00));

        /*
         * 10. VERIFY TRANSACTIONS WERE CREATED
         */
        mockMvc.perform(
                get("/api/accounts/"
                        + sourceAccountNumber
                        + "/transactions")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()")
                        .value(2));

        /*
         * 11. CREATE PAYMENT
         */
        String paymentRequest = """
                {
                    "sourceAccountNumber": "%s",
                    "destinationAccountNumber": "%s",
                    "amount": 100.00,
                    "type": "STANDARD",
                    "description": "Regression payment",
                    "idempotencyKey": "regression-payment-001"
                }
                """.formatted(
                        sourceAccountNumber,
                        destinationAccountNumber);

        mockMvc.perform(
                post("/api/payments")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount")
                        .value(100.00))
                .andExpect(jsonPath("$.type")
                        .value("STANDARD"));

        /*
         * 12. LOGOUT
         */
        mockMvc.perform(
                post("/api/auth/logout")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Logout successful"));

        /*
         * 13. REVOKED JWT MUST NO LONGER ACCESS PROTECTED API
         */
        mockMvc.perform(
                get("/api/auth/profile")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
