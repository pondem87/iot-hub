package com.pfitztronic.iothub.core.accounts.models;

import lombok.*;

import java.time.Instant;

@Builder
@AllArgsConstructor
@Getter
public class User {
    private PhoneNumber userId;
    private String name;
    private String passwordHash;
    private Instant createdAt;
    @Builder.Default
    private boolean verified = false;
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    public void changeName(String newName) {
        this.name = newName;
    }

    public void changePassword(String newPassword) {
        this.passwordHash = newPassword;
    }

    public void verify() {
        this.verified = true;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
    }
}