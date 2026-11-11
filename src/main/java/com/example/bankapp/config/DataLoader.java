package com.example.bankapp.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {

        if (customerRepository.count() == 0) {

            Customer kamal = new Customer();
            kamal.setAccountNumber("100001");
            kamal.setFullName("Kamal");
            kamal.setEmail("kamal@gmail.com");
            kamal.setBalance(new BigDecimal("50000.00"));

            Customer nimali = new Customer();
            nimali.setAccountNumber("100002");
            nimali.setFullName("Nimali");
            nimali.setEmail("nimali@gmail.com");
            nimali.setBalance(new BigDecimal("25000.00"));

            Customer saman = new Customer();
            saman.setAccountNumber("100003");
            saman.setFullName("Saman");
            saman.setEmail("saman@gmail.com");
            saman.setBalance(new BigDecimal("10000.00"));

            customerRepository.saveAll(
                List.of(kamal, nimali, saman)
            );

            System.out.println("Sample customers loaded successfully!");
        }
    }
}