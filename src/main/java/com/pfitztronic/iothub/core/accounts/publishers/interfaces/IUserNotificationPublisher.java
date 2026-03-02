package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

public interface IUserNotificationPublisher {
    public void publishUserNotificationEvent(String userId, String notificationMessage);
}
