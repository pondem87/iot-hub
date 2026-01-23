package com.pfitztronic.iothub.accounts.controllers;

import com.pfitztronic.iothub.accounts.services.AccountManagementService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountManagementController {
    private final AccountManagementService accountManagagementService;

    public AccountManagementController(
            AccountManagementService accountManagementService
    ) {
        this.accountManagagementService = accountManagementService;
    }

    @PostMapping("")
    public String createNewAccount() {
        return "New account created!";
    }

    @PatchMapping("")
    public String changeAccountDetails() {
        return "Account details updated!";
    }

    @PatchMapping("/disable")
    public String disableAccount() {
        return "Account disabled!";
    }

    @PatchMapping("/reactivate")
    public String reactivateAccount() {
        return "Account reactivated!";
    }

    @DeleteMapping("")
    public String permanentlyDeleteAccount() {
        return "Account permanently deleted!";
    }
}
