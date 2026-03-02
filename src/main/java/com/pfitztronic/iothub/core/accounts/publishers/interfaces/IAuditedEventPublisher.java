package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

public interface IAuditedEventPublisher {
    public void publishAuditedEvent(AuditedEvent message);
}
