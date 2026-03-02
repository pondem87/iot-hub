package com.pfitztronic.iothub.core.authentication.models;

import java.time.Instant;
import java.util.UUID;

public record Session(
        UUID sessionId,
        String userId,
        String userAgent,
        Instant createdAt,
        Instant expiresAt,
        Instant revokedAt
) {
}