package com.pfitztronic.iothub.core.authorisation.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    UUID id;
    UUID roleId;
    String roleName;
    String description;
    Boolean defaultRole;
    Instant createdAt;
    List<RolePermission> rolePermission;
}