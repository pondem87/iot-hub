package com.pfitztronic.iothub.core.accounts.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
public class AccountUser {
    private UUID id;
    private Account account;
    private User user;
    private List<AccountUserRole> roles;
    private AccountUserStatus status;
    private Instant joinedAt;

    public void enableUser() {
        this.status = AccountUserStatus.ACTIVE;
    }

    public void disableUser() {
        this.status = AccountUserStatus.DISABLED;
    }

    public void suspendUser() {
        this.status = AccountUserStatus.SUSPENDED;
    }

    public void addRole(UUID roleIdToAdd) {
        if (this.roles.stream().noneMatch(role -> role.getRoleId().equals(roleIdToAdd))) {
            this.roles.add(AccountUserRole.builder().roleId(roleIdToAdd).build());
        }
    }

    public void removeRole(UUID roleIdToRemove) {
        this.roles.removeIf(role -> role.getRoleId().equals(roleIdToRemove));
    }
}
