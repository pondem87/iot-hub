package com.pfitztronic.iothub.core.accounts.repositories.impl;

import com.pfitztronic.iothub.core.accounts.mappers.DomainOrmMapper;
import com.pfitztronic.iothub.core.accounts.models.VerificationCode;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IVerificationCodeRepository;

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

    public VerificationCode findLatestByUserId(String userId) {
        var entity = verificationCodeRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElse(null);
        return entity != null ? DomainOrmMapper.toVerificationCode(entity) : null;
    }
}
