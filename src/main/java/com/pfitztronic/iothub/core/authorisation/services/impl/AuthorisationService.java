package com.pfitztronic.iothub.core.authorisation.services.impl;

import com.pfitztronic.iothub.core.accounts.services.impl.UserDetailsImpl;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;

public class AuthorisationService {
    public void verifyAuthedUserId(String userId) {
        SecurityContext context = SecurityContextHolder.getContext();
        UserDetails authUserDetails = (UserDetails) Objects.requireNonNull(context.getAuthentication()).getPrincipal();
        assert authUserDetails != null;
        if (!authUserDetails.getUsername().equals(userId)) {
            throw new AuthorizationDeniedException("User is not authorised to perform this action");
        }
    }

    public void verifyAuthedUserIsAdmin(String accountId) {
        SecurityContext context = SecurityContextHolder.getContext();
        UserDetails authUserDetails = (UserDetails) Objects.requireNonNull(context.getAuthentication()).getPrincipal();
        assert authUserDetails != null;
        if (authUserDetails.getAuthorities().stream().noneMatch(
                a -> Objects.equals(
                        a.getAuthority(),
                        "ROLE_ADMIN:%s".formatted(accountId))
        )) {
            throw new AuthorizationDeniedException("User is not authorised to perform this action");
        }
    }

    public void isUserVerified() {
        SecurityContext context = SecurityContextHolder.getContext();
        UserDetailsImpl authUserDetails = (UserDetailsImpl) Objects.requireNonNull(context.getAuthentication()).getPrincipal();
        assert authUserDetails != null;
        if (!authUserDetails.isUserVerified()) {
            throw new AuthorizationDeniedException("User account is not verified");
        }
    }

}
