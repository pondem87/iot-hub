package com.pfitztronic.iothub.core.accounts.util;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CodeGenerator {

    public String generateCode() {
        try {
            int code = SecureRandom.getInstanceStrong().nextInt(1_000_000);
            return String.format("%06d", code);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to default SecureRandom if strong instance is not available
            SecureRandom secureRandom = new SecureRandom();
            int code = secureRandom.nextInt(1_000_000);
            return String.format("%06d", code);
        }
    }
}