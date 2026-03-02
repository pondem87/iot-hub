package com.pfitztronic.iothub.core.accounts.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
public class AccountUser {
    private UUID id;
    private Account account;
    private User user;
    private UUID roleId;
    private AccountUserStatus status;
    private Instant joinedAt;

    public void enableUser() {
        this.status = AccountUserStatus.ACTIVE;
    }

    public void suspendUser() {
        this.status = AccountUserStatus.SUSPENDED;
    }

    public void changeRole(UUID newRoleId) {
        this.roleId = newRoleId;
    }
}
