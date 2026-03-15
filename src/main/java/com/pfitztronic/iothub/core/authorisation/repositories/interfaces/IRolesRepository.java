package com.pfitztronic.iothub.core.authorisation.repositories.interfaces;

import com.pfitztronic.iothub.core.authorisation.orm_models.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IRolesRepository extends JpaRepository<RoleEntity, UUID> {
}
