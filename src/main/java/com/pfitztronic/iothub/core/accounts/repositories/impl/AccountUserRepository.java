package com.pfitztronic.iothub.core.accounts.repositories.impl;

import com.pfitztronic.iothub.core.accounts.mappers.DomainOrmMapper;
import com.pfitztronic.iothub.core.accounts.models.Account;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.orm_models.AccountUserEntity;
import com.pfitztronic.iothub.core.accounts.orm_models.AccountUserRolesEntity;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IAccountUserRepository;

import java.util.List;
import java.util.UUID;

public class AccountUserRepository {
    private final IAccountUserRepository baseRepository;

    public AccountUserRepository(IAccountUserRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    public List<User> getAccountUsers(UUID accountId) {
        return baseRepository.findByAccountAccountId(accountId).stream()
                .map(AccountUserEntity::getUser)
                .map(DomainOrmMapper::toUser)
                .toList();
    }

    public List<Account> getUserAccounts(String userId) {
        return baseRepository.findByUserUserId(userId).stream()
                .map(AccountUserEntity::getAccount)
                .map(DomainOrmMapper::toAccount)
                .toList();
    }

    public List<UUID> getRolesForUser(String userId) {
        return baseRepository.findByUserUserId(userId).stream()
                .flatMap(accountUserEntity -> accountUserEntity.getRoles().stream())
                .map(AccountUserRolesEntity::getId)
                .toList();
    }
}
