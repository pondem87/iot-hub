package com.pfitztronic.iothub.core.accounts.repositories.impl;

import com.pfitztronic.iothub.core.accounts.mappers.DomainOrmMapper;
import com.pfitztronic.iothub.core.accounts.models.VerificationCode;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IVerificationCodeRepository;

import java.util.Optional;
import java.util.UUID;

public class VerificationCodeRepository  {
    private final IVerificationCodeRepository verificationCodeRepository;

    public VerificationCodeRepository(IVerificationCodeRepository verificationCodeRepository) {
        this.verificationCodeRepository = verificationCodeRepository;
    }

    public VerificationCode save(VerificationCode code) {
        var entity = DomainOrmMapper.toVerificationCodeEntity(code);
        var savedEntity = verificationCodeRepository.save(entity);
        return DomainOrmMapper.toVerificationCode(savedEntity);
    }

    public Optional<VerificationCode> findLatestByUserId(String userId) {
        return verificationCodeRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(DomainOrmMapper::toVerificationCode);
    }

    public void clearCodesForUser(String userId) {
        verificationCodeRepository.deleteByUserId(userId);
    }

    public void deleteById(UUID id) {
        this.verificationCodeRepository.deleteById(id);
    }
}
