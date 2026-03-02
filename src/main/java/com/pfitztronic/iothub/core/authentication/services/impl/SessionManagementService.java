package com.pfitztronic.iothub.core.authentication.services.impl;

import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentSessionNotFoundException;
import com.pfitztronic.iothub.core.authentication.models.Session;
import com.pfitztronic.iothub.core.authentication.repositories.impl.SessionRepository;

import java.time.Instant;
import java.util.UUID;

public class SessionManagementService {
    private SessionRepository sessionRepository;

    public SessionManagementService(
            SessionRepository sessionRepository
    ) {
        this.sessionRepository = sessionRepository;
    }

    public Session findCurrentUserAgentSession(
            UUID sessionId,
            String userId,
            String userAgent
    ) {
        var session = sessionRepository.findCurentUserAgentSession(sessionId, userId, userAgent);
        if (session == null) {
            throw new UserAgentSessionNotFoundException("User agent session not found!");
        }
        return session;
    }

    public Session createUserAgentSession(
            String userId,
            String userAgent,
            Long sessionDurationSeconds
    ) {
        var session = new Session(
                null,
                userId,
                userAgent,
                null,
                Instant.now().plusSeconds(sessionDurationSeconds),
                null
        );

        return sessionRepository.save(session);
    }
}
