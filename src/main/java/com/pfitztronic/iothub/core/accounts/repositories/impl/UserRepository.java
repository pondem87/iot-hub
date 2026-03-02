package com.pfitztronic.iothub.core.accounts.repositories.impl;

import com.pfitztronic.iothub.core.accounts.mappers.DomainOrmMapper;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.orm_models.UserEntity;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IUserRepository;

public class UserRepository {
    private final IUserRepository repository;

    public UserRepository(IUserRepository repository) {
        this.repository = repository;
    }

    public User save(User user) {
        UserEntity userEntity = DomainOrmMapper.toUserEntity(user);
        UserEntity savedEntity = repository.save(userEntity);
        return DomainOrmMapper.toUserModel(savedEntity);
    }

    public User findUserById(String userId) {
        UserEntity userEntity = repository.findById(userId).orElse(null);
        if (userEntity == null) {
            return null;
        }
        return DomainOrmMapper.toUserModel(userEntity);
    }
}
