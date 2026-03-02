package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

import java.util.UUID;

public interface IAccountCreatedEventPublisher {
    public void publishAccountCreatedEvent(UUID accountId);
}
