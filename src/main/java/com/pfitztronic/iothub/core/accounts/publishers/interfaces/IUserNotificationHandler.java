package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

public interface IUserNotificationHandler {
    public void notification(String userId, String notificationMessage);
}
