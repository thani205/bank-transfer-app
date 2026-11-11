package com.example.bankapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.Customer;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;

    // Dashboard
    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) String keyword,
            Model model) {

        loadDashboard(model, keyword);

        return "bank";
    }

    // Add Customer
    @PostMapping("/customers")
    public String addCustomer(
            @ModelAttribute Customer customer,
            RedirectAttributes redirectAttributes) {

        try {
            bankService.createCustomer(customer);

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Customer added successfully");

        } catch (RuntimeException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }

        return "redirect:/";
    }

    // Transfer Money
    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute TransferRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            loadDashboard(model, null);
            return "bank";
        }

        try {
            bankService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.isSimulateFailure()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Transfer successful");

        } catch (RuntimeException ex) {

            auditService.recordFailure(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    ex.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "All changes were rolled back: " + ex.getMessage()
            );
        }

        return "redirect:/";
    }

    // Account Transaction History
    @GetMapping("/accounts/{accountNumber}/history")
    public String accountHistory(
            @PathVariable String accountNumber,
            Model model) {

        model.addAttribute("accountNumber", accountNumber);

        model.addAttribute(
                "transactions",
                bankService.getAccountHistory(accountNumber)
        );

        return "history";
    }

    // Dashboard Data
    private void loadDashboard(Model model, String keyword) {

        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute(
                    "customers",
                    bankService.searchCustomers(keyword)
            );
        } else {
            model.addAttribute(
                    "customers",
                    bankService.getAllCustomers()
            );
        }

        model.addAttribute(
                "transactions",
                bankService.getLatestTransactions()
        );

        model.addAttribute(
                "totalBalance",
                bankService.getTotalBalance()
        );

        model.addAttribute(
                "successCount",
                bankService.getSuccessCount()
        );

        model.addAttribute(
                "failedCount",
                bankService.getFailedCount()
        );

        model.addAttribute("transferRequest", new TransferRequest());

        model.addAttribute("customer", new Customer());
    }
}