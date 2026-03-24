package com.pfitztronic.iothub.core.accounts.repositories.impl;

import com.pfitztronic.iothub.core.accounts.mappers.DomainOrmMapper;
import com.pfitztronic.iothub.core.accounts.models.PasswordResetCode;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IPasswordResetCodeRepository;

import java.util.Optional;

/**
 * Repository implementation for password reset code persistence operations.
 * Provides a domain-object-centric interface for saving, retrieving, and deleting password reset codes.
 * Handles the mapping between ORM entities and domain models.
 */
public class PasswordResetCodeRepository {
    private final IPasswordResetCodeRepository passwordResetCodeRepository;

    /**
     * Constructs a PasswordResetCodeRepository with the required JPA repository.
     *
     * @param passwordResetCodeRepository the JPA repository for PasswordResetCodeEntity
     */
    public PasswordResetCodeRepository(IPasswordResetCodeRepository passwordResetCodeRepository) {
        this.passwordResetCodeRepository = passwordResetCodeRepository;
    }

    /**
     * Saves a password reset code to the database.
     *
     * @param code the PasswordResetCode domain model to save
     * @return the saved PasswordResetCode with generated ID and timestamps
     */
    public PasswordResetCode save(PasswordResetCode code) {
        var entity = DomainOrmMapper.toPasswordResetCodeEntity(code);
        var savedEntity = passwordResetCodeRepository.save(entity);
        return DomainOrmMapper.toPasswordResetCode(savedEntity);
    }

    /**
     * Retrieves the most recently created password reset code for a user.
     *
     * @param userId the phone number of the user
     * @return an Optional containing the latest PasswordResetCode if found, or empty if not found
     */
    public Optional<PasswordResetCode> findLatestByUserId(String userId) {
        return passwordResetCodeRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(DomainOrmMapper::toPasswordResetCode);
    }

    /**
     * Deletes all password reset codes associated with a user.
     * This is typically called before generating a new code to ensure only one valid code exists per user.
     *
     * @param userId the phone number of the user
     */
    public void clearCodesForUser(String userId) {
        passwordResetCodeRepository.deleteByUserId(userId);
    }

    /**
     * Deletes a password reset code by its unique identifier.
     *
     * @param id the UUID of the password reset code to delete
     */
    public void deleteById(java.util.UUID id) {
        passwordResetCodeRepository.deleteById(id);
    }
}

