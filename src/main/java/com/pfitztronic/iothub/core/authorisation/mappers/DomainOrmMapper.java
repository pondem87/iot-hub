package com.pfitztronic.iothub.core.authorisation.mappers;

import com.pfitztronic.iothub.core.authorisation.models.PermissionAction;
import com.pfitztronic.iothub.core.authorisation.models.PermissionEntity;
import com.pfitztronic.iothub.core.authorisation.models.Role;
import com.pfitztronic.iothub.core.authorisation.models.RolePermission;
import com.pfitztronic.iothub.core.authorisation.orm_models.RoleEntity;
import com.pfitztronic.iothub.core.authorisation.orm_models.RolePermissionEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DomainOrmMapper {
    public static Role toRoleModel(RoleEntity roleEntity) {
        var role = Role.builder()
                .id(roleEntity.getId())
                .accountId(roleEntity.getAccountId())
                .roleName(roleEntity.getRoleName())
                .description(roleEntity.getDescription())
                .defaultRole(roleEntity.getDefaultRole())
                .createdAt(roleEntity.getCreatedAt())
                .build();

        if (roleEntity.getRolePermissions() != null) {
            role.addRolePermissions(
                    roleEntity.getRolePermissions().stream()
                            .map(DomainOrmMapper::toRolePermissionModel)
                            .toList()
            );
        }

        return role;
    }

    public static RoleEntity toRoleEntity(Role role) {
        return RoleEntity.builder()
                .id(role.getId())
                .accountId(role.getAccountId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .rolePermissions(
                        role.getRolePermissions().stream()
                                .map(DomainOrmMapper::toRolePermissionEntity)
                                .toList()
                )
                .defaultRole(role.getDefaultRole())
                .createdAt(role.getCreatedAt())
                .build();
    }

    public static RolePermission toRolePermissionModel(RolePermissionEntity rolePermissionEntity) {
        return RolePermission.builder()
                .id(rolePermissionEntity.getId())
                .roleId(rolePermissionEntity.getRole().getId())
                .accountId(rolePermissionEntity.getAccountId())
                .entity(PermissionEntity.valueOf(rolePermissionEntity.getEntity()))
                .action(PermissionAction.valueOf(rolePermissionEntity.getAction()))
                .build();
    }

    public static RolePermissionEntity toRolePermissionEntity(RolePermission rolePermission) {
        return RolePermissionEntity.builder()
                .id(rolePermission.getId())
                .accountId(rolePermission.getAccountId())
                .entity(rolePermission.getEntity().name())
                .action(rolePermission.getAction().name())
                .build();
    }
}
