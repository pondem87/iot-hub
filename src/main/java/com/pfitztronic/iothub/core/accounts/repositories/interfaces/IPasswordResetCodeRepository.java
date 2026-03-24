package com.pfitztronic.iothub.core.accounts.repositories.interfaces;

import com.pfitztronic.iothub.core.accounts.orm_models.PasswordResetCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IPasswordResetCodeRepository extends JpaRepository<PasswordResetCodeEntity, UUID> {
    Optional<PasswordResetCodeEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);

    void deleteByUserId(String userId);
}

