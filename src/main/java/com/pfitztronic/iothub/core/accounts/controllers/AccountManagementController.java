package com.pfitztronic.iothub.core.accounts.controllers;

import com.pfitztronic.iothub.core.accounts.dto.CreateNewAccountData;
import com.pfitztronic.iothub.core.accounts.dto.CreateNewAccountForUserData;
import com.pfitztronic.iothub.core.accounts.dto.NewAccountResponse;
import com.pfitztronic.iothub.core.accounts.services.impl.AccountManagementService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountManagementController {
    private final AccountManagementService accountManagementService;

    public AccountManagementController(
            AccountManagementService accountManagementService
    ) {
        this.accountManagementService = accountManagementService;
    }

    @PostMapping()
    public NewAccountResponse createNewAccount(@Valid @RequestBody CreateNewAccountData data) {
        return this.accountManagementService.createNewAccount(data);
    }

    @PreAuthorize("@authService.verifyAuthedUserIsOwner(#data.userId())")
    @PostMapping()
    public NewAccountResponse createAccountForUser(
            @Valid @RequestBody CreateNewAccountForUserData data
    ) {
        return this.accountManagementService.createAccountForUser(data.userId(), data.accountName());
    }

    @PreAuthorize("@authService.verifyAuthedUserIsAdmin(#accountId)")
    @PatchMapping("{accountId}/disable")
    public String disableAccount(
            @PathVariable String accountId
    ) {
        return this.accountManagementService.disableAccount(accountId);
    }

    @PreAuthorize("@authService.verifyAuthedUserIsAdmin(#accountId)")
    @PatchMapping("{accountId}/reactivate")
    public String reactivateAccount(
            @PathVariable String accountId
    ) {
        return this.accountManagementService.reactivateAccount(accountId);
    }

    @PreAuthorize("@authService.verifyAuthedUserIsAdmin(#accountId)")
    @DeleteMapping("{accountId}")
    public String permanentlyDeleteAccount(
            @PathVariable String accountId
    ) {
        return this.accountManagementService.permanentlyDeleteAccount(accountId);
    }
}
