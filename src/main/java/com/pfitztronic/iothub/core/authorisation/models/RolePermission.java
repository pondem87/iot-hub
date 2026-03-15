package com.pfitztronic.iothub.core.authorisation.models;

import lombok.*;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RolePermission {
    UUID id;
    UUID roleId;
    UUID accountId;
    PermissionEntity entity;
    PermissionAction action;
}
