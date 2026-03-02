package com.pfitztronic.iothub.core.accounts.repositories.impl;

import com.pfitztronic.iothub.core.accounts.mappers.DomainOrmMapper;
import com.pfitztronic.iothub.core.accounts.models.Account;
import com.pfitztronic.iothub.core.accounts.orm_models.AccountEntity;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IAccountRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
public class AccountRepository {
    private final IAccountRepository repository;

    public AccountRepository(IAccountRepository repository) {
        this.repository = repository;
    }

    public Account save(Account account) {
        AccountEntity accountEntity = DomainOrmMapper.toAccountEntity(account);
        AccountEntity savedEntity = repository.save(accountEntity);
        return DomainOrmMapper.toAccountModel(savedEntity);
    }

    public Account findOneByName(String name) {
        AccountEntity accountEntity = repository.findByAccountNameIgnoreCase(name).orElse(null);
        return accountEntity != null ? DomainOrmMapper.toAccountModel(accountEntity) : null;
    }

    public Account findOneById(UUID id) {
        AccountEntity accountEntity = repository.findById(id).orElse(null);
        return accountEntity != null ? DomainOrmMapper.toAccountModel(accountEntity) : null;
    }

    public List<Account> findAllMarkedForDeletion() {
        Instant instant;
        List<AccountEntity> entities = repository.findAllByMarkedForDeletionAtBefore(Instant.now());
        return entities.stream().map(DomainOrmMapper::toAccountModel).toList();
    }

    public long findCountByAdminId(String adminId) {
        return repository.countByAdminId(adminId);
    }

    public boolean delete(Account account) {
        try {
            repository.deleteById(account.getAccountId());
            return true;
        } catch (Exception e) {
            log.warn("Failed to delete account with id {}: {}", account.getAccountId(), e.getMessage());
            return false;
        }
    }
}
