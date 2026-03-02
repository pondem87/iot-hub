package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

public interface IAccountStatusChangedEventPublisher {
    public void publishAccountStatusChangedEvent(AccountStatusChangedEvent message);
}
