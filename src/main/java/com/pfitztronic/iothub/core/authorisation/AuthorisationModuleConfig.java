package com.pfitztronic.iothub.core.authorisation;

import com.pfitztronic.iothub.core.accounts.services.interfaces.IUserPermissionsService;
import com.pfitztronic.iothub.core.authorisation.repositories.impl.RolesRepository;
import com.pfitztronic.iothub.core.authorisation.repositories.interfaces.IRolesRepository;
import com.pfitztronic.iothub.core.authorisation.services.impl.AccountCreatedEventHandler;
import com.pfitztronic.iothub.core.authorisation.services.impl.AuthorisationService;
import com.pfitztronic.iothub.core.authorisation.services.impl.UserPermissionsService;
import com.pfitztronic.iothub.core.authorisation.services.impl.UserRolesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthorisationModuleConfig {

    @Bean
    public IUserPermissionsService userPermissionsService() {
        return new UserPermissionsService();
    }

    @Bean("authService")
    public AuthorisationService authorisationService() {
        return new AuthorisationService();
    }

    @Bean
    public RolesRepository rolesRepository(
            IRolesRepository baseRepository
    ) {
        return new RolesRepository(baseRepository);
    }

    @Bean
    public UserRolesService userRolesService(
            RolesRepository rolesRepository
    ) {
        return new UserRolesService(rolesRepository);
    }

    @Bean
    public AccountCreatedEventHandler accountCreatedEventHandler(
            UserRolesService userRolesService
    ) {
        return new AccountCreatedEventHandler(userRolesService);
    }
}
