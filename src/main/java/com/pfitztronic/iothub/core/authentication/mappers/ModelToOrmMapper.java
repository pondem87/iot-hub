package com.pfitztronic.iothub.core.authentication.mappers;

import com.pfitztronic.iothub.core.authentication.models.Session;
import com.pfitztronic.iothub.core.authentication.orm_models.SessionEntity;

public class ModelToOrmMapper {
    public static SessionEntity toSessionEntity(Session session) {
        return SessionEntity.builder()
                .sessionId(session.sessionId())
                .userId(session.userId())
                .userAgent(session.userAgent())
                .createdAt(session.createdAt())
                .expiresAt(session.expiresAt())
                .revokedAt(session.revokedAt())
                .build();
    }

    public static Session toSession(SessionEntity sessionEntity) {
        return new Session(
                sessionEntity.getSessionId(),
                sessionEntity.getUserId(),
                sessionEntity.getUserAgent(),
                sessionEntity.getCreatedAt(),
                sessionEntity.getExpiresAt(),
                sessionEntity.getRevokedAt()
        );
    }
}
