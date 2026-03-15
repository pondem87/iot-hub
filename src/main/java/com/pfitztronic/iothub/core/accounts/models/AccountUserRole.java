package com.pfitztronic.iothub.core.accounts.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
public class AccountUserRole {
    private UUID id;
    private UUID roleId;
}
