package com.pfitztronic.iothub.core.authorisation.services.impl;

import com.pfitztronic.iothub.core.accounts.services.interfaces.IUserPermissionsService;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserPermissionsService implements IUserPermissionsService {
    @Override
    public Collection<GrantedAuthority> getUserPermissions(List<UUID> roleIds) {
        return List.of();
    }
}
