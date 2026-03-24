package com.pfitztronic.iothub.core.accounts.services.impl;

import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.services.interfaces.IUserPermissionsService;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;


@NullMarked
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserManagementService userManagementService;
    private final IUserPermissionsService userPermissionsService;
    private final AccountUserManagementService accountUserManagementService;

    public UserDetailsServiceImpl(
            UserManagementService userManagementService,
            IUserPermissionsService userPermissionsService,
            AccountUserManagementService accountUserManagementService
            ) {
        this.userManagementService = userManagementService;
        this.userPermissionsService = userPermissionsService;
        this.accountUserManagementService = accountUserManagementService;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userManagementService.getUserById(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with id: " + username);
        }

        var roleIds = accountUserManagementService.getRolesForUser(user.get().getUserId().number());

        var permissions = userPermissionsService.getUserPermissions(roleIds);

        return new UserDetailsImpl(user.get(), permissions);
    }
}
