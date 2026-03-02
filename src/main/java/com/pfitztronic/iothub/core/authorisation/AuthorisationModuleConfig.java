package com.pfitztronic.iothub.core.authorisation;

import com.pfitztronic.iothub.core.accounts.services.interfaces.IUserPermissionsService;
import com.pfitztronic.iothub.core.authorisation.services.impl.AuthorisationService;
import com.pfitztronic.iothub.core.authorisation.services.impl.UserPermissionsService;
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

}
