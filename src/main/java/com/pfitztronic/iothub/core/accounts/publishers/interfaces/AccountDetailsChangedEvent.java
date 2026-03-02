package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

import java.util.Map;
import java.util.UUID;

public record AccountDetailsChangedEvent(
        UUID accountId,
        Map<String, String> changedDetails
) {
}
