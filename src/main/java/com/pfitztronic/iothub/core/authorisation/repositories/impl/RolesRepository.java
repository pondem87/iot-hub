package com.pfitztronic.iothub.core.authorisation.repositories.impl;

import com.pfitztronic.iothub.core.authorisation.mappers.DomainOrmMapper;
import com.pfitztronic.iothub.core.authorisation.models.Role;
import com.pfitztronic.iothub.core.authorisation.repositories.interfaces.IRolesRepository;

public class RolesRepository {
    private final IRolesRepository baseRepository;

    public RolesRepository(IRolesRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    public Role save(Role role) {
        return DomainOrmMapper.toRoleModel(
            baseRepository.save(DomainOrmMapper.toRoleEntity(role))
        );
    }
}
