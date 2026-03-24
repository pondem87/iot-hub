package com.pfitztronic.iothub.core.accounts.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
public class PasswordResetCode {
    private UUID id;
    private PhoneNumber userId;
    private String codeHash;
    private Instant expiresAt;
    private Instant createdAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
