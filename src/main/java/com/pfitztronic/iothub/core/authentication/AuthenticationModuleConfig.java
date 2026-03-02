package com.pfitztronic.iothub.core.authentication;

import com.pfitztronic.iothub.core.authentication.repositories.impl.SessionRepository;
import com.pfitztronic.iothub.core.authentication.repositories.interfaces.ISessionRepository;
import com.pfitztronic.iothub.core.authentication.services.impl.JwtAuthenticationFilter;
import com.pfitztronic.iothub.core.authentication.services.impl.JwtAuthenticationProvider;
import com.pfitztronic.iothub.core.authentication.services.impl.JwtAuthenticationSessionService;
import com.pfitztronic.iothub.core.authentication.services.impl.SessionManagementService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableConfigurationProperties({JwtProperties.class})
@Configuration
public class AuthenticationModuleConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter (
            AuthenticationManager authenticationManager
    ) {
        return new JwtAuthenticationFilter(authenticationManager);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            JwtProperties jwtProperties,
            ISessionRepository baseSessionRepository,
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);

        JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(
                new JwtAuthenticationSessionService(
                        jwtProperties,
                        new SessionManagementService(
                                new SessionRepository(baseSessionRepository)
                        )
                ),
                userDetailsService
        );

        return new ProviderManager(
                daoAuthenticationProvider,
                jwtAuthenticationProvider
        );
    }

}
