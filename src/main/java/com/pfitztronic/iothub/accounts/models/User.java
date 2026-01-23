package com.pfitztronic.iothub.accounts.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private PhoneNumber phoneNumber;
    private String name;
    private Instant createdAt;
    private boolean verified;
    private String passwordHash;
    private UserStatus status;
}