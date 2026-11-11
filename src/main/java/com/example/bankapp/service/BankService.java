package com.example.bankapp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankapp.exception.AccountNotFoundException;
import com.example.bankapp.exception.InsufficientBalanceException;
import com.example.bankapp.exception.InvalidTransactionException;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    // Get all customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // Search customers by name
    public List<Customer> searchCustomers(String keyword) {
        return customerRepository.searchByName(keyword);
    }

    // Get all customers ordered by name
    public List<Customer> getCustomersOrdered() {
        return customerRepository.findAllOrdered();
    }

    // Get total bank balance
    public BigDecimal getTotalBalance() {
        return customerRepository.getTotalBalance();
    }

    // Get latest transactions
    public List<BankTransaction> getLatestTransactions() {
        return transactionRepository.findAllLatest();
    }

    // Get account transaction history
    public List<BankTransaction> getAccountHistory(String accountNumber) {
        return transactionRepository.findHistory(accountNumber);
    }

    // Count successful transactions
    public long getSuccessCount() {
        return transactionRepository.countByStatus(
                TransactionStatus.SUCCESS);
    }

    // Count failed transactions
    public long getFailedCount() {
        return transactionRepository.countByStatus(
                TransactionStatus.FAILED);
    }

    // Create customer
    @Transactional
    public Customer createCustomer(Customer customer) {

        if (customerRepository.countAccounts(
                customer.getAccountNumber()) > 0) {
            throw new InvalidTransactionException(
                    "Account number already exists");
        }

        if (customerRepository.countEmails(
                customer.getEmail()) > 0) {
            throw new InvalidTransactionException(
                    "Email already exists");
        }

        return customerRepository.save(customer);
    }

    // Bank transfer
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            boolean simulateFailure) {

        if (fromAccount.equals(toAccount)) {
            throw new InvalidTransactionException(
                    "Sender and receiver accounts must be different");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                    "Transfer amount must be greater than zero");
        }

        Customer sender = customerRepository
                .getByAccountNumber(fromAccount)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Sender account not found"));

        Customer receiver = customerRepository
                .getByAccountNumber(toAccount)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Receiver account not found"));

        // Step 1: Debit sender account
        int debited = customerRepository.debit(fromAccount, amount);

        if (debited == 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        // Step 2: Simulate system failure
        if (simulateFailure) {
            throw new InvalidTransactionException(
                    "Simulated system failure");
        }

        // Step 3: Credit receiver account
        int credited = customerRepository.credit(toAccount, amount);

        if (credited == 0) {
            throw new AccountNotFoundException(
                    "Receiver account not found");
        }

        // Step 4: Save successful transaction
        BankTransaction transaction = new BankTransaction();

        transaction.setAmount(amount);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setDescription("Bank transfer");
        transaction.setStatus(TransactionStatus.SUCCESS);

        return transactionRepository.save(transaction);
    }
}