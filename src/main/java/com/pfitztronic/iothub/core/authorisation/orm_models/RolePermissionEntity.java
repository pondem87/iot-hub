package com.pfitztronic.iothub.core.authorisation.orm_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "role_permissions")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "role_id", nullable = false)
    private UUID roleId;
    private UUID accountId;
    private String entity;
    private String action;
    @ManyToOne
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private RoleEntity role;
}
