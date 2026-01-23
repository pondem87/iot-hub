package com.pfitztronic.iothub.accounts.services;
import org.springframework.stereotype.Service;

@Service
public class AccountManagementService {
    public String createNewAccount() {
        return "New account created!";
    }

    public String changeAccountDetails() {
        return "Account details updated!";
    }

    public String disableAccount() {
        return "Account disabled!";
    }

    public String reactivateAccount() {
        return "Account reactivated!";
    }

    public String permanentlyDeleteAccount() {
        return "Account permanently deleted!";
    }
}
