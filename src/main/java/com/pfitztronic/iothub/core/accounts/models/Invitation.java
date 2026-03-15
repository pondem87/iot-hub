package com.pfitztronic.iothub.core.accounts.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
public class Invitation {
    UUID invitationId;
    PhoneNumber userId;
    UUID accountId;
    @Builder.Default
    InvitationStatus invitationStatus = InvitationStatus.PENDING;
    Instant createdAt;

    public void cancelInvitation() {
        if (this.invitationStatus == InvitationStatus.REVOKED) {
            throw new IllegalArgumentException("Invitation is in REVOKED state");
        }

        if (this.invitationStatus == InvitationStatus.ACCEPTED) {
            throw new IllegalArgumentException("Invitation is in ACCEPTED state");
        }

        this.invitationStatus = InvitationStatus.CANCELLED;
    }

    public void acceptInvitation() {
        if (this.invitationStatus != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation cannot be accepted");
        }

        this.invitationStatus = InvitationStatus.ACCEPTED;
    }

    public void revokeInvitation() {
        this.invitationStatus = InvitationStatus.REVOKED;
    }
}
