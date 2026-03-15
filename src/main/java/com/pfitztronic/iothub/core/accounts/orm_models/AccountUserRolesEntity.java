package com.pfitztronic.iothub.core.accounts.orm_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "account_user_roles")
public class AccountUserRolesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name="account_user_id", nullable=false)
    private AccountUserEntity accountUser;
    @Column(nullable = false)
    private UUID roleId;
}
