package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

public interface IUserCreatedEventPublisher {
    public void publishUserCreatedEvent(String userId);
}
