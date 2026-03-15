package com.pfitztronic.iothub.core.accounts.publishers.impl;

import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IAccountDeletedEventHandler;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IAccountDeletedEventPublisher;

import java.util.List;
import java.util.UUID;

public class AccountDeletedEventPublisher implements IAccountDeletedEventPublisher {

    private List<IAccountDeletedEventHandler> handlers;

    public AccountDeletedEventPublisher(List<IAccountDeletedEventHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void publishAccountDeletedEvent(UUID accountID) {
        for (IAccountDeletedEventHandler handler : handlers) {
            handler.accountDeletedEvent(accountID);
        }
    }

    @Override
    public void subscribe(IAccountDeletedEventHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void unsubscribe(IAccountDeletedEventHandler handler) {
        handlers.remove(handler);
    }
}
