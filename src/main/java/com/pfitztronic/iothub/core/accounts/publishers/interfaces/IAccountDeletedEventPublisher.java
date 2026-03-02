package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

import java.util.UUID;

public interface IAccountDeletedEventPublisher {
    public void publishAccountDeletedEvent(UUID accountID);
}
