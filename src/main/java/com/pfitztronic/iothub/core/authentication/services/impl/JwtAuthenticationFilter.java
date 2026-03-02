package com.pfitztronic.iothub.core.authentication.services.impl;

import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentSessionAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@NullMarked
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.trace("Checking for JWT in request header.");

        // get JWT from request header and tenant id
        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        var userAgent = request.getHeader(HttpHeaders.USER_AGENT);

        // get bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.trace("JWT authentication header not found.");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // authenticate JWT using authentication manager
            var token = authHeader.replace("Bearer ", "");
            var authenticationRequest = new JwtAuthentication(token, userAgent);
            var authenticationResult = authenticationManager.authenticate(authenticationRequest);

            log.trace("JWT authentication result: {}", authenticationResult.isAuthenticated());

            // set authentication in security context
            if (authenticationResult.isAuthenticated()) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authenticationResult);
                SecurityContextHolder.setContext(context);
            }
        } catch (UserAgentSessionAuthenticationException e) {
            log.trace("Jwt exception: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
