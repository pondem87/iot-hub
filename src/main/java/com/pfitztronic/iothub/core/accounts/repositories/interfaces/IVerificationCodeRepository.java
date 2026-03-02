package com.pfitztronic.iothub.core.accounts.repositories.interfaces;

import com.pfitztronic.iothub.core.accounts.orm_models.VerificationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IVerificationCodeRepository extends JpaRepository<VerificationCodeEntity, UUID> {
    public Optional<VerificationCodeEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);
}
