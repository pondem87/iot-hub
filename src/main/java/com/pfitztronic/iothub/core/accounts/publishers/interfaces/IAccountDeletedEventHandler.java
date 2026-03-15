package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

import java.util.UUID;

public interface IAccountDeletedEventHandler {
    public void accountDeletedEvent(UUID accountId);
}
