package com.pfitztronic.iothub.core.authentication.services.impl;

public record JwtAuthenticationCredentials(
    String token,
    String userAgent
) {
}
