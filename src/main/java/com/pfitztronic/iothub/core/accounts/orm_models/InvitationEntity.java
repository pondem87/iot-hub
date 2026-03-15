package com.pfitztronic.iothub.core.accounts.orm_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "invitations")
public class InvitationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID invitationId;
    @Column(nullable = false)
    UUID accountId;
    @Column(length = 50, nullable = false)
    String userId;
    @Column(length = 50, nullable = false)
    String invitationStatus;
    Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
