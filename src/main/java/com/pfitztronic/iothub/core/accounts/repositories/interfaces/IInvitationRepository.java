package com.pfitztronic.iothub.core.accounts.repositories.interfaces;

import com.pfitztronic.iothub.core.accounts.orm_models.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IInvitationRepository extends JpaRepository<InvitationEntity, UUID> {
    List<InvitationEntity> findByAccountId(UUID accountId);
    Optional<InvitationEntity> findByUserId(String userId);
    List<InvitationEntity> findAllByUserId(String userId);
}
