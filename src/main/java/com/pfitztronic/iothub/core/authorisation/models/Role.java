package com.pfitztronic.iothub.core.authorisation.models;

import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
public class Role {
    UUID id;
    UUID accountId;
    String roleName;
    String description;
    @Builder.Default
    Boolean defaultRole = false;
    Instant createdAt;
    @Builder.Default
    List<RolePermission> rolePermissions = new ArrayList<>();

    public void makeDefault() {
        if (id != null) {
            throw new IllegalStateException("Cannot make persisted role default");
        }

        this.defaultRole = true;
    }

    public void addRolePermissions(List<RolePermission> rolePermissionsToAdd) {
       rolePermissions.addAll(rolePermissionsToAdd);
    }
}