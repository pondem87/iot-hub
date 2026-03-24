package com.pfitztronic.iothub.core.authentication.services.impl;

import com.pfitztronic.iothub.core.authentication.dto.LoginInputData;
import com.pfitztronic.iothub.core.authentication.dto.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtAuthenticationSessionService jwtAuthenticationSessionService;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            JwtAuthenticationSessionService jwtAuthenticationSessionService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtAuthenticationSessionService = jwtAuthenticationSessionService;
    }

    public LoginResponse login(String userAgent, LoginInputData credentials) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                credentials.phoneNumber(),
                credentials.password()
        );

        Authentication authentication = authenticationManager.authenticate(authToken);

        if (authentication.isAuthenticated()) {
            String token = jwtAuthenticationSessionService.generateToken(authentication.getName(), userAgent);
            return new LoginResponse(token);
        } else {
            throw new AuthorizationDeniedException("Authentication failed");
        }
    }

    public String logout(String userAgent) {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() != null && context.getAuthentication().isAuthenticated()) {
            String userId = context.getAuthentication().getName();
            jwtAuthenticationSessionService.revokeCurrentSession(userId, userAgent);
            return "Logged out successfully";
        } else {
            throw new AuthorizationDeniedException("No authenticated user found");
        }
    }

    public String logoutAllSessions() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() != null && context.getAuthentication().isAuthenticated()) {
            String userId = context.getAuthentication().getName();
            jwtAuthenticationSessionService.revokeAllSessions(userId);
            return "Logged out from all sessions successfully";
        } else {
            throw new AuthorizationDeniedException("No authenticated user found");
        }
    }
}
