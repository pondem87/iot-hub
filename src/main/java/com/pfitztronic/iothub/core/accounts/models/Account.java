package com.pfitztronic.iothub.core.accounts.models;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
public class Account {
    private UUID accountId;
    private AccountName accountName;
    private PhoneNumber adminId;
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;
    private Instant markedForDeletionAt;
    private Instant createdAt;

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    public void markForDeletionAt(Instant markForDeletionAt) {
        this.markedForDeletionAt = markForDeletionAt;
        this.status = AccountStatus.DISABLED;
    }

    public void activate() {
        this.markedForDeletionAt = null;
        this.status = AccountStatus.ACTIVE;
    }

    public void suspend() {
        this.status = AccountStatus.SUSPENDED;
    }

    public void disable() {
        this.status = AccountStatus.DISABLED;
    }

}
