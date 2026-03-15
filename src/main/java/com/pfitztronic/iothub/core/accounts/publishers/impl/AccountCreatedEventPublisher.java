package com.pfitztronic.iothub.core.accounts.publishers.impl;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IAccountCreatedEventHandler;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IAccountCreatedEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountCreatedEventPublisher implements IAccountCreatedEventPublisher {

    private final List<IAccountCreatedEventHandler> handlers;

    public AccountCreatedEventPublisher(List<IAccountCreatedEventHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void publishAccountCreatedEvent(UUID accountId) {
        for (IAccountCreatedEventHandler handler : handlers) {
            handler.accountCreatedEvent(accountId);
        }
    }

    @Override
    public void subscribe(IAccountCreatedEventHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void unsubscribe(IAccountCreatedEventHandler handler) {
        handlers.remove(handler);
    }
}
