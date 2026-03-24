package com.pfitztronic.iothub.core.accounts.services;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "accounts-config")
public record AccountsConfigProperties(
    int deleteAccountDelayDays,
    int verifyAccountDelayHours,
    int passwordResetDelayHours
) {
}
