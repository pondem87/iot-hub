package com.pfitztronic.iothub.accounts.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private UUID accountId;
    private String accountName;
    private PhoneNumber adminId;
    private AccountStatus status;
    private Instant markedForDeletionAt;
    private Instant createdAt;
}
