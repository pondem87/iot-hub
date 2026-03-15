package com.pfitztronic.iothub.core.authorisation.repositories.interfaces;

import com.pfitztronic.iothub.core.authorisation.orm_models.RoleEntity;
import com.pfitztronic.iothub.core.authorisation.orm_models.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IPermissionsRepository extends JpaRepository<RolePermissionEntity, UUID> {
}
