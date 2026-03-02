package com.pfitztronic.iothub.core.authentication.repositories.interfaces;

import com.pfitztronic.iothub.core.authentication.orm_models.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ISessionRepository extends JpaRepository<SessionEntity, UUID> {
    Optional<SessionEntity> findFirstBySessionIdAndUserIdAndUserAgentAndRevokedAtIsNullOrderByCreatedAtDesc(UUID sessionId, String userId, String userAgent);
}
