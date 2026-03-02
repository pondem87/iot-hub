package com.pfitztronic.iothub.core.authentication.services.impl;
import com.pfitztronic.iothub.core.authentication.JwtProperties;
import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentCredentialsInvalidException;
import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentSessionExpiredException;
import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentSessionRevokedException;
import com.pfitztronic.iothub.core.authentication.models.Session;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JwtAuthenticationSessionService {
    private static final String SESSION_ID_CLAIM = "sessionId";
    private static final String USER_ID_CLAIM = "userId";
    private static final String USER_AGENT_CLAIM = "userAgent";

    private final JwtProperties jwtProperties;
    private final SessionManagementService sessionManagementService;

    public JwtAuthenticationSessionService(
            JwtProperties jwtProperties,
            SessionManagementService sessionManagementService
    ) {
        this.jwtProperties = jwtProperties;
        this.sessionManagementService = sessionManagementService;
    }

    public String authenticate(String token) {
        Map<String, String> sessionInfo = validateAndDecodeToken(token);
        Session session = getSession(
                sessionInfo.get(SESSION_ID_CLAIM),
                sessionInfo.get(USER_ID_CLAIM),
                sessionInfo.get(USER_AGENT_CLAIM)
        );


        return validateSession(session);
    }

    private Map<String, String> validateAndDecodeToken(String token) {
        // Dummy token validation
        if (token == null || token.isEmpty()) {
            throw new UserAgentCredentialsInvalidException("Invalid token");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            var sessionId = claims.get(SESSION_ID_CLAIM, String.class);
            var userId = claims.getSubject();
            var userAgent = claims.get(USER_AGENT_CLAIM, String.class);

            return new HashMap<String, String>() {{
                put(SESSION_ID_CLAIM, sessionId);
                put(USER_ID_CLAIM, userId);
                put(USER_AGENT_CLAIM, userAgent);
            }};
        } catch (MalformedJwtException | SignatureException e) {
            throw new UserAgentCredentialsInvalidException("User agent credentials are invalid");
        }

    }

    private Session getSession(String sessionId, String userId, String userAgent) {
        UUID sessionIdUuid;

        try {
            sessionIdUuid = UUID.fromString(sessionId);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid session ID format");
        }

        return sessionManagementService.findCurrentUserAgentSession(
                sessionIdUuid,
                userId,
                userAgent
        );
    }

    private String validateSession(Session session) {
        // check session expiry
        if (session.expiresAt().isBefore(java.time.Instant.now())) {
            throw new UserAgentSessionExpiredException("Token has expired");
        }
        // check session revocation
        if (session.revokedAt() != null) {
            throw new UserAgentSessionRevokedException("Token has been revoked");
        }

        return session.userId();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
    }

    public String generateToken(String userId, String userAgent) {
        Session session = createSession(userId, userAgent, jwtProperties.expiration());

        return Jwts.builder()
                .subject(session.userId())
                .claim(SESSION_ID_CLAIM, session.sessionId().toString())
                .claim(USER_AGENT_CLAIM, session.userAgent())
                .issuedAt(Date.from(session.createdAt()))
                .expiration(java.util.Date.from(session.expiresAt()))
                .signWith(this.getSigningKey())
                .compact();
    }

    private Session createSession(String userId, String userAgent, long durationSeconds) {
        return sessionManagementService.createUserAgentSession(
                userId,
                userAgent,
                durationSeconds
        );
    }
}
