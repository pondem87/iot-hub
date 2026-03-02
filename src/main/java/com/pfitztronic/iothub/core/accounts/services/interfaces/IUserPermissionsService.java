package com.pfitztronic.iothub.core.accounts.services.interfaces;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface IUserPermissionsService {
    public Collection<GrantedAuthority> getUserPermissions(String userId);
}
