package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

public interface IAccountDetailsChangedEventPublisher {
    public void publishAccountDetailsChangedEvent(AccountDetailsChangedEvent message);
}
