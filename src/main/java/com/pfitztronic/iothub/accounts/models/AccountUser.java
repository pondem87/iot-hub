package com.pfitztronic.iothub.accounts.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountUser {
    public UUID id;
    private UUID accountId;
    private PhoneNumber userId;
    private UUID roleId;
    private AccountStatus status;
    private Instant joinedAt;
}
