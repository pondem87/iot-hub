package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.services.interfaces.IUserPermissionsService;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@NullMarked
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserManagementService userManagementService;
    private final IUserPermissionsService userPermissionsService;

    public UserDetailsServiceImpl(
            UserManagementService userManagementService,
            IUserPermissionsService userPermissionsService
            ) {
        this.userManagementService = userManagementService;
        this.userPermissionsService = userPermissionsService;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userManagementService.getUserById(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with id: " + username);
        }

        var permissions = userPermissionsService.getUserPermissions(username);

        return new UserDetailsImpl(user, permissions);
    }
}
