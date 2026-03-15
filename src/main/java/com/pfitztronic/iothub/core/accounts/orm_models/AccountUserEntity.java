package com.pfitztronic.iothub.core.accounts.orm_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="account_users")
public class AccountUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    @ManyToOne
    @JoinColumn(name="account_id", nullable=false)
    private AccountEntity account;
    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private UserEntity user;
    @OneToMany(mappedBy = "accountUser")
    private List<AccountUserRolesEntity> roles;
    @Column(length = 50, nullable = false)
    private String status;
    @Column(nullable = false, updatable = false)
    private java.time.Instant joinedAt;

    @PrePersist
    void onCreate() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }
}
