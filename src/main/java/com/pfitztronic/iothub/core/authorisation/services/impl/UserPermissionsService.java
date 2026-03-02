package com.pfitztronic.iothub.core.authorisation.services.impl;

import com.pfitztronic.iothub.core.accounts.services.interfaces.IUserPermissionsService;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public class UserPermissionsService implements IUserPermissionsService {
    @Override
    public List<GrantedAuthority> getUserPermissions(String userId) {
        return List.of();
    }
}
