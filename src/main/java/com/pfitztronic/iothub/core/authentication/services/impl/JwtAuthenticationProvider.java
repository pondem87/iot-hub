package com.pfitztronic.iothub.core.authentication.services.impl;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

@NullMarked
public class JwtAuthenticationProvider implements AuthenticationProvider {
    public JwtAuthenticationSessionService jwtAuthenticationSessionService;
    public UserDetailsService userDetailsService;

    public JwtAuthenticationProvider(
            JwtAuthenticationSessionService jwtAuthenticationSessionService,
            UserDetailsService userDetailsService
    ) {
        this.jwtAuthenticationSessionService = jwtAuthenticationSessionService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtAuthenticationCredentials jwtAuthenticationCredentials = (JwtAuthenticationCredentials) authentication.getCredentials();
        assert jwtAuthenticationCredentials != null;
        String userId = jwtAuthenticationSessionService.authenticate(
                jwtAuthenticationCredentials.token()
        );
        var userDetails = userDetailsService.loadUserByUsername(userId);
        return new JwtAuthentication(userDetails, true);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }
}
