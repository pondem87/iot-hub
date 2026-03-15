package com.pfitztronic.iothub.core.accounts.publishers.interfaces;

public interface IAuditedEventHandler {
    public void auditedEvent(AuditedEvent message);
}
