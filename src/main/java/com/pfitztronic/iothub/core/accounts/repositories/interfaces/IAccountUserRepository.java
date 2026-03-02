package com.pfitztronic.iothub.core.accounts.repositories.interfaces;

import com.pfitztronic.iothub.core.accounts.orm_models.AccountUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IAccountUserRepository extends JpaRepository<AccountUserEntity, UUID> {
    List<AccountUserEntity> findByAccountId(UUID accountId);

    List<AccountUserEntity> findByUserId(String userId);
}
