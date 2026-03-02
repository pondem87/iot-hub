package com.pfitztronic.iothub.core.authentication.repositories.impl;

import com.pfitztronic.iothub.core.authentication.mappers.ModelToOrmMapper;
import com.pfitztronic.iothub.core.authentication.models.Session;
import com.pfitztronic.iothub.core.authentication.orm_models.SessionEntity;
import com.pfitztronic.iothub.core.authentication.repositories.interfaces.ISessionRepository;

import java.util.Optional;
import java.util.UUID;

public class SessionRepository {
    private final ISessionRepository baseRepository;

    public SessionRepository(
            ISessionRepository baseRepository
    ) {
        this.baseRepository = baseRepository;
    }

    public Session findCurentUserAgentSession(
            UUID sessionId,
            String userId,
            String userAgent
    ) {
        Optional<SessionEntity> entity = baseRepository.findFirstBySessionIdAndUserIdAndUserAgentAndRevokedAtIsNullOrderByCreatedAtDesc(
                sessionId,
                userId,
                userAgent
        );

        return entity.map(ModelToOrmMapper::toSession).orElse(null);
    }

    public Session save(Session session) {
        SessionEntity entity = ModelToOrmMapper.toSessionEntity(session);
        SessionEntity savedEntity = baseRepository.save(entity);
        return ModelToOrmMapper.toSession(savedEntity);
    }
}
