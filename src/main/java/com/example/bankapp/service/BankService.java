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

    // Get All Customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllOrdered();
    }

    // Search Customers
    public List<Customer> searchCustomers(String keyword) {
        return customerRepository.searchByName(keyword);
    }

    // Get Latest Transactions
    public List<BankTransaction> getLatestTransactions() {
        return transactionRepository.findAllLatest();
    }

    // Get Total Balance
    public BigDecimal getTotalBalance() {
        return customerRepository.getTotalBalance();
    }

    // Get Successful Transaction Count
    public long getSuccessCount() {
        return transactionRepository.countByStatus(TransactionStatus.SUCCESS);
    }

    // Get Failed Transaction Count
    public long getFailedCount() {
        return transactionRepository.countByStatus(TransactionStatus.FAILED);
    }

    // Get Account History
    public List<BankTransaction> getAccountHistory(String accountNumber) {
        return transactionRepository.findHistory(accountNumber);
    }

    // Create Customer
    @Transactional
    public Customer createCustomer(Customer customer) {

        if (customerRepository.existsByAccountNumber(customer.getAccountNumber())) {
            throw new RuntimeException("Account number already exists");
        }

        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        return customerRepository.save(customer);
    }

    // Transfer Money
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
                .orElseThrow(() -> new AccountNotFoundException(
                        "Sender account not found"));

        Customer receiver = customerRepository
                .getByAccountNumber(toAccount)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Receiver account not found"));

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // Debit sender
        sender.setBalance(sender.getBalance().subtract(amount));
        customerRepository.save(sender);

        // Simulate system failure
        if (simulateFailure) {
            throw new RuntimeException("Simulated system failure");
        }

        // Credit receiver
        receiver.setBalance(receiver.getBalance().add(amount));
        customerRepository.save(receiver);

        // Create successful transaction record
        BankTransaction transaction = new BankTransaction();

        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setDescription("Bank transfer");
        transaction.setStatus(TransactionStatus.SUCCESS);

        return transactionRepository.save(transaction);
    }
}