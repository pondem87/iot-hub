package com.pfitztronic.iothub.core.authorisation.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolePermission {
    UUID id;
    UUID roleId;
    UUID accountId;
    PermissionEntity entity;
    PermissionAction action;
}
