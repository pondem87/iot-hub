package com.pfitztronic.iothub.core.accounts.repositories.interfaces;

import com.pfitztronic.iothub.core.accounts.orm_models.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAccountRepository extends JpaRepository<AccountEntity, UUID> {
    Optional<AccountEntity> findByAccountNameIgnoreCase(String accountName);
    long countByAdminId(String adminId);
    List<AccountEntity> findAllByMarkedForDeletionAtBefore(Instant instant);
}
