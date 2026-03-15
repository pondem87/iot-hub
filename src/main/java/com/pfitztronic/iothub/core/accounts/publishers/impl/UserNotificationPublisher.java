package com.pfitztronic.iothub.core.accounts.publishers.impl;

import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserNotificationHandler;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserNotificationPublisher;

import java.util.List;

public class UserNotificationPublisher implements IUserNotificationPublisher {

    private final List<IUserNotificationHandler> handlers;

    public UserNotificationPublisher(List<IUserNotificationHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void publishUserNotificationEvent(String userId, String notificationMessage) {
        for (IUserNotificationHandler handler : handlers) {
            handler.notification(userId, notificationMessage);
        }
    }

    @Override
    public void subscribe(IUserNotificationHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void unsubscribe(IUserNotificationHandler handler) {
        handlers.remove(handler);
    }
}
