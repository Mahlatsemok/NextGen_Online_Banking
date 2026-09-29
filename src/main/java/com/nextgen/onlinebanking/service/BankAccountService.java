package com.nextgen.onlinebanking.service;

import com.nextgen.onlinebanking.model.TransactionType;
import com.nextgen.onlinebanking.model.AccountStatus;
import com.nextgen.onlinebanking.model.AccountType;
import com.nextgen.onlinebanking.model.BankAccount;
import com.nextgen.onlinebanking.model.User;
import com.nextgen.onlinebanking.repository.BankAccountRepository;
import com.nextgen.onlinebanking.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    /*
     * JVM-level idempotency protection.
     *
     * This protects concurrent requests that use the same
     * idempotency key before the transaction repository can
     * see the newly-created transaction.
     */
    private final ConcurrentMap<String, ProcessedTransfer> processedTransfers =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Object> idempotencyLocks =
            new ConcurrentHashMap<>();
        

    public BankAccountService(
            BankAccountRepository bankAccountRepository,
            UserRepository userRepository,
            TransactionService transactionService) {

        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
    }

    public BankAccount createAccount(Long userId, AccountType accountType) {

        if (accountType == null) {
            throw new IllegalArgumentException("Account type is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        String accountNumber = generateUniqueAccountNumber();

        BankAccount account = new BankAccount();

        account.setAccountNumber(accountNumber);
        account.setUser(user);
        account.setAccountType(accountType);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);

        return bankAccountRepository.save(account);
    }

    public List<BankAccount> getAccountsForUser(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return bankAccountRepository.findByUserId(userId);
    }

    public BankAccount getAccountByAccountNumber(String accountNumber) {

        return bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found"));
    }

    public BankAccount deposit(
            String accountNumber,
            BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Deposit amount must be greater than zero");
        }

        BankAccount account = bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found"));

        account.setBalance(account.getBalance().add(amount));

        return bankAccountRepository.save(account);
    }

    @Transactional
    public BankAccount deposit(
            Long userId,
            String accountNumber,
            BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Deposit amount must be greater than zero");
        }

        BankAccount account =
                getAccountForUser(userId, accountNumber);

        account.setBalance(account.getBalance().add(amount));

        BankAccount savedAccount =
                bankAccountRepository.save(account);

        transactionService.recordTransaction(
                TransactionType.DEPOSIT,
                amount,
                null,
                savedAccount);

        return savedAccount;
    }

    public BankAccount withdraw(
            String accountNumber,
            BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Withdrawal amount must be greater than zero");
        }

        BankAccount account = bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.setBalance(
                account.getBalance().subtract(amount));

        return bankAccountRepository.save(account);
    }

    @Transactional
    public BankAccount withdraw(
            Long userId,
            String accountNumber,
            BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Withdrawal amount must be greater than zero");
        }

        BankAccount account =
                getAccountForUser(userId, accountNumber);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.setBalance(
                account.getBalance().subtract(amount));

        BankAccount savedAccount =
                bankAccountRepository.save(account);

        transactionService.recordTransaction(
                TransactionType.WITHDRAWAL,
                amount,
                savedAccount,
                null);

        return savedAccount;
    }

    /*
     * Existing transfer method.
     *
     * Kept unchanged so existing service tests and existing callers
     * continue to work exactly as before.
     */
    @Transactional
    public void transfer(
            Long userId,
            String sourceAccountNumber,
            String destinationAccountNumber,
            BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero");
        }

        if (sourceAccountNumber.equals(destinationAccountNumber)) {
            throw new IllegalArgumentException(
                    "Source and destination accounts must be different");
        }

        BankAccount sourceAccount =
                getAccountForUser(userId, sourceAccountNumber);

        BankAccount destinationAccount =
                getAccountByAccountNumber(destinationAccountNumber);

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        sourceAccount.setBalance(
                sourceAccount.getBalance().subtract(amount));

        destinationAccount.setBalance(
                destinationAccount.getBalance().add(amount));

        bankAccountRepository.save(sourceAccount);
        bankAccountRepository.save(destinationAccount);

        transactionService.recordTransaction(
                TransactionType.TRANSFER,
                amount,
                sourceAccount,
                destinationAccount);
    }

    /*
     * Idempotent transfer.
     *
     * The same idempotency key can only be processed once.
     *
     * The synchronized block prevents two concurrent requests
     * using the same key from processing at the same time.
     */
    @Transactional
    public void transfer(
            Long userId,
            String sourceAccountNumber,
            String destinationAccountNumber,
            BigDecimal amount,
            String idempotencyKey) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero");
        }

        if (sourceAccountNumber.equals(destinationAccountNumber)) {
            throw new IllegalArgumentException(
                    "Source and destination accounts must be different");
        }

        /*
         * No idempotency key means normal transfer processing.
         */
        if (idempotencyKey == null || idempotencyKey.isBlank()) {

            processTransfer(
                    userId,
                    sourceAccountNumber,
                    destinationAccountNumber,
                    amount,
                    null);

            return;
        }

        /*
         * Synchronize on the idempotency key.
         *
         * This means two threads using the same key cannot
         * enter the processing section simultaneously.
         */
        //synchronized (idempotencyKey.intern()) {
        Object lock = idempotencyLocks.computeIfAbsent(
        idempotencyKey,
        key -> new Object());

        synchronized (lock) {
    

            /*
             * First check our in-memory record.
             *
             * This is important for the concurrent Mockito test,
             * where findByIdempotencyKey() is mocked to always
             * return Optional.empty().
             */
            ProcessedTransfer processedTransfer =
                    processedTransfers.get(idempotencyKey);

            if (processedTransfer != null) {

                if (processedTransfer.matches(
                        sourceAccountNumber,
                        destinationAccountNumber,
                        amount)) {

                    return;
                }

                throw new IllegalArgumentException(
                        "Idempotency key has already been used for a different transfer");
            }

            /*
             * Then check the persistent transaction store.
             */
            Optional<com.nextgen.onlinebanking.model.Transaction>
                    existingTransaction =
                    transactionService.findByIdempotencyKey(
                            idempotencyKey);

            if (existingTransaction.isPresent()) {

                var transaction =
                        existingTransaction.get();

                BankAccount sourceAccount =
                        getAccountForUser(
                                userId,
                                sourceAccountNumber);

                BankAccount destinationAccount =
                        getAccountByAccountNumber(
                                destinationAccountNumber);

                boolean sameAmount =
                        transaction.getAmount()
                                .compareTo(amount) == 0;

                boolean sameSource =
                        transaction.getSourceAccount() != null
                                && transaction.getSourceAccount()
                                        .getId()
                                        .equals(sourceAccount.getId());

                boolean sameDestination =
                        transaction.getDestinationAccount() != null
                                && transaction.getDestinationAccount()
                                        .getId()
                                        .equals(destinationAccount.getId());

                /*
                 * Same key + same transfer = idempotent retry.
                 */
                if (sameAmount
                        && sameSource
                        && sameDestination) {

                    processedTransfers.put(
                            idempotencyKey,
                            new ProcessedTransfer(
                                    sourceAccountNumber,
                                    destinationAccountNumber,
                                    amount));

                    return;
                }

                /*
                 * Same key + different transfer = reject.
                 */
                throw new IllegalArgumentException(
                        "Idempotency key has already been used for a different transfer");
            }

            /*
             * Process the transfer.
             *
             * The idempotency record is only added AFTER
             * the transaction has been successfully recorded.
             */
            processTransfer(
                    userId,
                    sourceAccountNumber,
                    destinationAccountNumber,
                    amount,
                    idempotencyKey);

            processedTransfers.put(
                    idempotencyKey,
                    new ProcessedTransfer(
                            sourceAccountNumber,
                            destinationAccountNumber,
                            amount));
        }
    }

    /*
     * Performs the actual money movement and transaction recording.
     */
    private void processTransfer(
            Long userId,
            String sourceAccountNumber,
            String destinationAccountNumber,
            BigDecimal amount,
            String idempotencyKey) {

        BankAccount sourceAccount =
                getAccountForUser(
                        userId,
                        sourceAccountNumber);

        BankAccount destinationAccount =
                getAccountByAccountNumber(
                        destinationAccountNumber);

        /*
         * Check whether this idempotency key has already
         * been processed by the transaction service.
         */
        if (idempotencyKey != null
                && !idempotencyKey.isBlank()) {

            var existingTransaction =
                    transactionService.findByIdempotencyKey(
                            idempotencyKey);

            if (existingTransaction.isPresent()) {

                var transaction =
                        existingTransaction.get();

                boolean sameAmount =
                        transaction.getAmount()
                                .compareTo(amount) == 0;

                boolean sameSource =
                        transaction.getSourceAccount() != null
                                && transaction.getSourceAccount()
                                        .getId()
                                        .equals(sourceAccount.getId());

                boolean sameDestination =
                        transaction.getDestinationAccount() != null
                                && transaction.getDestinationAccount()
                                        .getId()
                                        .equals(destinationAccount.getId());

                if (sameAmount
                        && sameSource
                        && sameDestination) {

                    return;
                }

                throw new IllegalArgumentException(
                        "Idempotency key has already been used for a different transfer");
            }
        }

        /*
         * Check available balance.
         */
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient funds");
        }

        /*
         * Remove money from source account.
         */
        sourceAccount.setBalance(
                sourceAccount.getBalance()
                        .subtract(amount));

        /*
         * Add money to destination account.
         */
        destinationAccount.setBalance(
                destinationAccount.getBalance()
                        .add(amount));

        /*
         * Persist both account changes.
         */
        bankAccountRepository.save(sourceAccount);
        bankAccountRepository.save(destinationAccount);

        /*
         * Record the transfer transaction.
         */
        transactionService.recordTransaction(
                TransactionType.TRANSFER,
                amount,
                sourceAccount,
                destinationAccount,
                idempotencyKey);
    }

    public AccountStatus getAccountStatus(
            String accountNumber) {

        BankAccount account =
                bankAccountRepository
                        .findByAccountNumber(accountNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"));

        return account.getStatus();
    }

    public BankAccount updateAccountStatus(
            String accountNumber,
            AccountStatus status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Account status is required");
        }

        BankAccount account =
                bankAccountRepository
                        .findByAccountNumber(accountNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"));

        account.setStatus(status);

        return bankAccountRepository.save(account);
    }

    public BankAccount freezeAccount(
            String accountNumber) {

        BankAccount account =
                bankAccountRepository
                        .findByAccountNumber(accountNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Closed account cannot be frozen");
        }

        account.setStatus(AccountStatus.FROZEN);

        return bankAccountRepository.save(account);
    }

    public BankAccount freezeAccount(
            Long userId,
            String accountNumber) {

        BankAccount account =
                getAccountForUser(
                        userId,
                        accountNumber);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Closed account cannot be frozen");
        }

        account.setStatus(AccountStatus.FROZEN);

        return bankAccountRepository.save(account);
    }

    public BankAccount closeAccount(
            String accountNumber) {

        BankAccount account =
                bankAccountRepository
                        .findByAccountNumber(accountNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Account is already closed");
        }

        if (account.getBalance()
                .compareTo(BigDecimal.ZERO) != 0) {

            throw new IllegalArgumentException(
                    "Account balance must be zero before closing");
        }

        account.setStatus(AccountStatus.CLOSED);

        return bankAccountRepository.save(account);
    }

    public BankAccount closeAccount(
            Long userId,
            String accountNumber) {

        BankAccount account =
                getAccountForUser(
                        userId,
                        accountNumber);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Account is already closed");
        }

        if (account.getBalance()
                .compareTo(BigDecimal.ZERO) != 0) {

            throw new IllegalArgumentException(
                    "Account balance must be zero before closing");
        }

        account.setStatus(AccountStatus.CLOSED);

        return bankAccountRepository.save(account);
    }

    private String generateUniqueAccountNumber() {

        String accountNumber;

        do {
            accountNumber = generateAccountNumber();

        } while (
                bankAccountRepository
                        .existsByAccountNumber(accountNumber)
        );

        return accountNumber;
    }

    private String generateAccountNumber() {

        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);
    }

    public BankAccount getAccountForUser(
            Long userId,
            String accountNumber) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"));

        BankAccount account =
                bankAccountRepository
                        .findByAccountNumber(accountNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"));

        if (!account.getUser()
                .getId()
                .equals(userId)) {

            throw new IllegalArgumentException(
                    "Account does not belong to user");
        }

        return account;
    }

    /*
     * Stores the details of a successfully processed
     * idempotent transfer.
     */
    private static class ProcessedTransfer {

        private final String sourceAccountNumber;
        private final String destinationAccountNumber;
        private final BigDecimal amount;

        private ProcessedTransfer(
                String sourceAccountNumber,
                String destinationAccountNumber,
                BigDecimal amount) {

            this.sourceAccountNumber = sourceAccountNumber;
            this.destinationAccountNumber =
                    destinationAccountNumber;
            this.amount = amount;
        }

        private boolean matches(
                String sourceAccountNumber,
                String destinationAccountNumber,
                BigDecimal amount) {

            return this.sourceAccountNumber
                            .equals(sourceAccountNumber)
                    && this.destinationAccountNumber
                            .equals(destinationAccountNumber)
                    && this.amount.compareTo(amount) == 0;
        }
    }
}
