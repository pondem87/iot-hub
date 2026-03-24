package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.accounts.repositories.impl.AccountUserRepository;
import com.pfitztronic.iothub.core.accounts.util.CodeGenerator;

import java.util.List;
import java.util.UUID;

public class AccountUserManagementService {
    private final AccountUserRepository accountUserRepository;
    private final CodeGenerator codeGenerator;

    public AccountUserManagementService(AccountUserRepository accountUserRepository) {
        this.accountUserRepository = accountUserRepository;
        this.codeGenerator = new CodeGenerator();
    }

    public List<UUID> getRolesForUser(String userId) {
        return accountUserRepository.getRolesForUser(userId);
    }
}
