package com.nextgen.onlinebanking.controller;

import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.model.UserRole;
import com.nextgen.onlinebanking.repository.UserRepository;
import com.nextgen.onlinebanking.security.JwtService;
import com.nextgen.onlinebanking.model.AccountStatus;
import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.repository.BankAccountRepository;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class BankAccountAuthorizationIntegrationTest {

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

   @BeforeEach
   void setUp() {
       bankAccountRepository.deleteAll();
       bankAccountRepository.flush();

       userRepository.deleteAll();
       userRepository.flush();
   }


    @Test
    void shouldRejectRequestWithoutToken() throws Exception {

        mockMvc.perform(
                get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedUserToAccessOwnAccounts()
            throws Exception {

        User user = new User(
                "John",
                "User",
                "john@example.com",
                passwordEncoder.encode("Password123"));

        user.setRole(UserRole.USER);

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                get("/api/accounts")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isOk());
    }
    
    @Test
    void shouldReturnAuthenticatedUsersAccounts() throws Exception {

        User user = new User(
                "John",
                "User",
                "john@example.com",
                passwordEncoder.encode("Password123"));

        user.setRole(UserRole.USER);

        userRepository.save(user);

        BankAccount account = new BankAccount();

        account.setAccountNumber("1234567890");
        account.setUser(user);
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("1500.00"));
        account.setStatus(AccountStatus.ACTIVE);

        bankAccountRepository.save(account);

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                get("/api/accounts")
                        .header(
                                "Authorization",
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].accountNumber")
                                .value("1234567890"))
                .andExpect(
                        jsonPath("$[0].balance")
                                .value(1500.00))
                .andExpect(
                        jsonPath("$[0].status")
                                .value("ACTIVE"));
    }

    @Test
    void shouldDepositMoneyIntoAuthenticatedUsersAccount()
            throws Exception {

        User user = new User(
                "John",
                "User",
                "john@example.com",
                passwordEncoder.encode("Password123"));

        user.setRole(UserRole.USER);

        userRepository.save(user);

        BankAccount account = new BankAccount();

        account.setAccountNumber("1234567890");
        account.setUser(user);
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        bankAccountRepository.save(account);

        String token = jwtService.generateToken(user.getEmail());

        String requestBody = """
                {
                    "amount": 500.00
                }
                """;

        mockMvc.perform(
                post("/api/accounts/1234567890/deposit")
                        .header(
                                "Authorization",
                                "Bearer " + token)
                        .contentType(
                                "application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.accountNumber")
                                .value("1234567890"))
                .andExpect(
                        jsonPath("$.balance")
                                .value(1500.00))
                .andExpect(
                        jsonPath("$.status")
                                .value("ACTIVE"));
    }
    
    @Test
void shouldWithdrawMoneyFromAuthenticatedUsersAccount()
        throws Exception {

    User user = new User(
            "John",
            "User",
            "john@example.com",
            passwordEncoder.encode("Password123"));

    user.setRole(UserRole.USER);

    userRepository.save(user);

    BankAccount account = new BankAccount();

    account.setAccountNumber("1234567890");
    account.setUser(user);
    account.setAccountType(AccountType.CHECKING);
    account.setBalance(new BigDecimal("1500.00"));
    account.setStatus(AccountStatus.ACTIVE);

    bankAccountRepository.save(account);

    String token = jwtService.generateToken(user.getEmail());

    String requestBody = """
            {
                "amount": 500.00
            }
            """;

    mockMvc.perform(
            post("/api/accounts/1234567890/withdraw")
                    .header(
                            "Authorization",
                            "Bearer " + token)
                    .contentType(
                            "application/json")
                    .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(
                    jsonPath("$.accountNumber")
                            .value("1234567890"))
            .andExpect(
                    jsonPath("$.balance")
                            .value(1000.00))
            .andExpect(
                    jsonPath("$.status")
                            .value("ACTIVE"));
    }
    
    @Test
void shouldRejectWithdrawalWhenFundsAreInsufficient()
        throws Exception {

    User user = new User(
            "John",
            "User",
            "john@example.com",
            passwordEncoder.encode("Password123"));

    user.setRole(UserRole.USER);

    userRepository.save(user);

    BankAccount account = new BankAccount();

    account.setAccountNumber("1234567890");
    account.setUser(user);
    account.setAccountType(AccountType.CHECKING);
    account.setBalance(new BigDecimal("500.00"));
    account.setStatus(AccountStatus.ACTIVE);

    bankAccountRepository.save(account);

    String token = jwtService.generateToken(user.getEmail());

    String requestBody = """
            {
                "amount": 1000.00
            }
            """;

    mockMvc.perform(
            post("/api/accounts/1234567890/withdraw")
                    .header(
                            "Authorization",
                            "Bearer " + token)
                    .contentType(
                            "application/json")
                    .content(requestBody))
            .andExpect(status().isConflict());
}
    
@Test
void shouldFreezeAuthenticatedUsersAccount()
        throws Exception {

    User user = new User(
            "John",
            "User",
            "john@example.com",
            passwordEncoder.encode("Password123"));

    user.setRole(UserRole.USER);

    userRepository.save(user);

    BankAccount account = new BankAccount();

    account.setAccountNumber("1234567890");
    account.setUser(user);
    account.setAccountType(AccountType.CHECKING);
    account.setBalance(new BigDecimal("1000.00"));
    account.setStatus(AccountStatus.ACTIVE);

    bankAccountRepository.save(account);

    String token = jwtService.generateToken(user.getEmail());

    mockMvc.perform(
            post("/api/accounts/1234567890/freeze")
                    .header(
                            "Authorization",
                            "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(
                    jsonPath("$.accountNumber")
                            .value("1234567890"))
            .andExpect(
                    jsonPath("$.balance")
                            .value(1000.00))
            .andExpect(
                    jsonPath("$.status")
                            .value("FROZEN"));
    }

     @Test
void shouldRejectFreezingClosedAccount()
        throws Exception {

    User user = new User(
            "John",
            "User",
            "john@example.com",
            passwordEncoder.encode("Password123"));

    user.setRole(UserRole.USER);

    userRepository.save(user);

    BankAccount account = new BankAccount();

    account.setAccountNumber("1234567890");
    account.setUser(user);
    account.setAccountType(AccountType.CHECKING);
    account.setBalance(BigDecimal.ZERO);
    account.setStatus(AccountStatus.CLOSED);

    bankAccountRepository.save(account);

    String token = jwtService.generateToken(user.getEmail());

    mockMvc.perform(
            post("/api/accounts/1234567890/freeze")
                    .header(
                            "Authorization",
                            "Bearer " + token))
            .andExpect(status().isConflict());
}

  @Test
void shouldCloseAuthenticatedUsersAccount()
        throws Exception {

    User user = new User(
            "John",
            "User",
            "john@example.com",
            passwordEncoder.encode("Password123"));

    user.setRole(UserRole.USER);

    userRepository.save(user);

    BankAccount account = new BankAccount();

    account.setAccountNumber("1234567890");
    account.setUser(user);
    account.setAccountType(AccountType.CHECKING);
    account.setBalance(BigDecimal.ZERO);
    account.setStatus(AccountStatus.ACTIVE);

    bankAccountRepository.save(account);

    String token = jwtService.generateToken(user.getEmail());

    mockMvc.perform(
            post("/api/accounts/1234567890/close")
                    .header(
                            "Authorization",
                            "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(
                    jsonPath("$.accountNumber")
                            .value("1234567890"))
            .andExpect(
                    jsonPath("$.balance")
                            .value(0.00))
            .andExpect(
                    jsonPath("$.status")
                            .value("CLOSED"));
}
    
@Test
void shouldRejectClosingAccountWithNonZeroBalance()
        throws Exception {

    User user = new User(
            "John",
            "User",
            "john@example.com",
            passwordEncoder.encode("Password123"));

    user.setRole(UserRole.USER);

    userRepository.save(user);

    BankAccount account = new BankAccount();

    account.setAccountNumber("1234567890");
    account.setUser(user);
    account.setAccountType(AccountType.CHECKING);
    account.setBalance(new BigDecimal("1000.00"));
    account.setStatus(AccountStatus.ACTIVE);

    bankAccountRepository.save(account);

    String token = jwtService.generateToken(user.getEmail());

    mockMvc.perform(
            post("/api/accounts/1234567890/close")
                    .header(
                            "Authorization",
                            "Bearer " + token))
            .andExpect(status().isConflict());
}

@Test
void shouldRejectClosingAlreadyClosedAccount()
        throws Exception {

    User user = new User(
            "John",
            "User",
            "john@example.com",
            passwordEncoder.encode("Password123"));

    user.setRole(UserRole.USER);

    userRepository.save(user);

    BankAccount account = new BankAccount();

    account.setAccountNumber("1234567890");
    account.setUser(user);
    account.setAccountType(AccountType.CHECKING);
    account.setBalance(BigDecimal.ZERO);
    account.setStatus(AccountStatus.CLOSED);

    bankAccountRepository.save(account);

    String token = jwtService.generateToken(user.getEmail());

    mockMvc.perform(
            post("/api/accounts/1234567890/close")
                    .header(
                            "Authorization",
                            "Bearer " + token))
            .andExpect(status().isConflict());
}
 
@Test
void shouldRejectUserFromAccessingAnotherUsersAccount()
        throws Exception {

    // Create account owner
    User owner = new User(
            "John",
            "Owner",
            "owner@example.com",
            passwordEncoder.encode("Password123"));

    owner.setRole(UserRole.USER);
    userRepository.save(owner);

    // Create another user
    User otherUser = new User(
            "Jane",
            "Other",
            "other@example.com",
            passwordEncoder.encode("Password123"));

    otherUser.setRole(UserRole.USER);
    userRepository.save(otherUser);

    // Create account belonging to owner
    BankAccount account = new BankAccount();

    account.setAccountNumber("1234567890");
    account.setUser(owner);
    account.setAccountType(AccountType.CHECKING);
    account.setBalance(new BigDecimal("1000.00"));
    account.setStatus(AccountStatus.ACTIVE);

    bankAccountRepository.save(account);

    // Authenticate as the OTHER user
    String token = jwtService.generateToken(otherUser.getEmail());

    // Other user attempts to access owner's account
    mockMvc.perform(
            get("/api/accounts/1234567890")
                    .header(
                            "Authorization",
                            "Bearer " + token))
            .andExpect(status().isConflict());
}

}
