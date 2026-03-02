package com.pfitztronic.iothub.core.accounts.util;

import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderProxy {
    private final PasswordEncoder encoder;

    public PasswordEncoderProxy(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
