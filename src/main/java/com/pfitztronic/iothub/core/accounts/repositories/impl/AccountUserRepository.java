package com.pfitztronic.iothub.core.accounts.repositories.impl;

import com.pfitztronic.iothub.core.accounts.mappers.DomainOrmMapper;
import com.pfitztronic.iothub.core.accounts.models.Account;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.orm_models.AccountUserEntity;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IAccountUserRepository;

import java.util.List;
import java.util.UUID;

public class AccountUserRepository {
    private IAccountUserRepository baseRepository;

    public AccountUserRepository(IAccountUserRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    public List<User> getAccountUsers(UUID accountId) {
        return baseRepository.findByAccountId(accountId).stream()
                .map(AccountUserEntity::getUser)
                .map(DomainOrmMapper::toUserModel)
                .toList();
    }

    public List<Account> getUserAccounts(String userId) {
        return baseRepository.findByUserId(userId).stream()
                .map(AccountUserEntity::getAccount)
                .map(DomainOrmMapper::toAccountModel)
                .toList();
    }
}
