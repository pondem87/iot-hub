package com.pfitztronic.iothub.core.accounts.services.interfaces;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IUserPermissionsService {
    public Collection<GrantedAuthority> getUserPermissions(List<UUID> roleIds);
}
