package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

import java.time.Instant;
import java.util.UUID;

public record AuditedEvent(
        UUID accountId,
        String userId,
        Instant timestamp,
        String objectType,
        String objectId,
        String description,
        String source
) {
}
