package com.pfitztronic.iothub.core.accounts.orm_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="password_reset_codes")
public class PasswordResetCodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    @Column(length = 50, nullable = false)
    private String userId;
    @Column(nullable = false)
    private String codeHash;
    Instant createdAt;
    @Column(nullable = false)
    Instant expiresAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
