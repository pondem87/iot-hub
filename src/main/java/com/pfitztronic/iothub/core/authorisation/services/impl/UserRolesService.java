package com.pfitztronic.iothub.core.authorisation.services.impl;

import com.pfitztronic.iothub.core.authorisation.models.Role;
import com.pfitztronic.iothub.core.authorisation.repositories.impl.RolesRepository;

import java.util.UUID;

public class UserRolesService {
    private final RolesRepository rolesRepository;

    public UserRolesService(RolesRepository rolesRepository) {
        this.rolesRepository = rolesRepository;
    }

    public void createDefaultRolesForAccount(UUID accountId) {
       Role oberserverRole = Role.builder()
                .accountId(accountId)
                .roleName("Observer")
                .description("Can view all resources but cannot make any changes.")
                .build();

       oberserverRole.makeDefault();

       this.rolesRepository.save(oberserverRole);
    }
}
