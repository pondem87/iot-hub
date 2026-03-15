package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

import java.util.UUID;

public interface IAccountCreatedEventHandler {
    public void accountCreatedEvent(UUID accountId);
}
