package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.accounts.repositories.impl.AccountUserRepository;

import java.util.List;
import java.util.UUID;

public class AccountUserManagementService {
    private final AccountUserRepository accountUserRepository;

    public AccountUserManagementService(AccountUserRepository accountUserRepository) {
        this.accountUserRepository = accountUserRepository;
    }

    public List<UUID> getRolesForUser(String userId) {
        return accountUserRepository.getRolesForUser(userId);
    }
}
