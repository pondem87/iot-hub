package com.pfitztronic.iothub.accounts.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCode {
    String id;
    PhoneNumber userId;
    String code;
    Instant createdAt;
}
