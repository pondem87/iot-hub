package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

import java.util.UUID;

public record AccountStatusChangedEvent(
        UUID accountId,
        String newStatus
) {
}
