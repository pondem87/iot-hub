package com.pfitztronic.iothub.core.accounts.repositories.impl;

import com.pfitztronic.iothub.core.accounts.mappers.DomainOrmMapper;
import com.pfitztronic.iothub.core.accounts.models.Invitation;
import com.pfitztronic.iothub.core.accounts.orm_models.InvitationEntity;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IInvitationRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InvitationRepository {
    private final IInvitationRepository repository;

    public InvitationRepository(IInvitationRepository repository) {
        this.repository = repository;
    }

    public Optional<Invitation> getInvitationById(UUID invitationId) {
        return repository.findById(invitationId)
                .map(DomainOrmMapper::toInvitation);
    }

    public List<Invitation> findInvitationsByAccount(UUID accountId) {
        return repository.findByAccountId(accountId).stream()
                .map(DomainOrmMapper::toInvitation)
                .toList();
    }

    public Optional<Invitation> getInvitationByUser(String userId) {
        return repository.findByUserId(userId)
                .map(DomainOrmMapper::toInvitation);
    }

    public List<Invitation> findInvitationsByUser(String userId) {
        return repository.findAllByUserId(userId).stream()
                .map(DomainOrmMapper::toInvitation)
                .toList();
    }
}
