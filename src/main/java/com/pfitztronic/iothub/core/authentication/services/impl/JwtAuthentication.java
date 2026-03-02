package com.pfitztronic.iothub.core.authentication.services.impl;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class JwtAuthentication implements Authentication {
    private final JwtAuthenticationCredentials credentials;
    private final UserDetails userDetails;
    private final Boolean authenticated;

    public JwtAuthentication(String token, String userAgent) {
        this.credentials = new JwtAuthenticationCredentials(token, userAgent);
        this.authenticated = false;
        this.userDetails = null;
    }

    public JwtAuthentication(UserDetails userDetails, Boolean authenticated) {
        this.credentials = null;
        this.userDetails = userDetails;
        this.authenticated = authenticated;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userDetails != null ? userDetails.getAuthorities() : List.of();
    }

    @Override
    public @Nullable Object getCredentials() {
        return credentials;
    }

    @Override
    public @Nullable Object getDetails() {
        return userDetails;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return userDetails;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new IllegalArgumentException("Cannot change authentication state");
    }

    @Override
    public String getName() {
        return userDetails != null ? userDetails.getUsername() : "";
    }

}
