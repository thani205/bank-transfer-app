package com.example.bankapp.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankapp.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Get customer by account number
    Optional<Customer> getByAccountNumber(String accountNumber);

    // Check whether account number already exists
    boolean existsByAccountNumber(String accountNumber);

    // Check whether email already exists
    boolean existsByEmail(String email);

    // Count all accounts
    @Query("SELECT COUNT(c) FROM Customer c")
    long countAccounts();

    // Count customers with a specific email
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.email = :email")
    long countEmails(@Param("email") String email);

    // Find all customers ordered by name
    @Query("SELECT c FROM Customer c ORDER BY c.fullName ASC")
    List<Customer> findAllOrdered();

    // Search customers by name
    @Query("SELECT c FROM Customer c WHERE LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Customer> searchByName(@Param("keyword") String keyword);

    // Get total balance
    @Query("SELECT COALESCE(SUM(c.balance), 0) FROM Customer c")
    BigDecimal getTotalBalance();

    // Native query to count customers
    @Query(value = "SELECT COUNT(*) FROM customers", nativeQuery = true)
    long countCustomersNative();

    // Debit customer account
    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.balance = c.balance - :amount WHERE c.accountNumber = :accountNumber")
    int debit(
            @Param("accountNumber") String accountNumber,
            @Param("amount") BigDecimal amount
    );

    // Credit customer account
    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.balance = c.balance + :amount WHERE c.accountNumber = :accountNumber")
    int credit(
            @Param("accountNumber") String accountNumber,
            @Param("amount") BigDecimal amount
    );
}