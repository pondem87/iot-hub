package com.pfitztronic.iothub.core.accounts.repositories.impl;

import com.pfitztronic.iothub.core.accounts.mappers.DomainOrmMapper;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.orm_models.UserEntity;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IUserRepository;

import java.util.Optional;

public class UserRepository {
    private final IUserRepository repository;

    public UserRepository(IUserRepository repository) {
        this.repository = repository;
    }

    public User save(User user) {
        UserEntity userEntity = DomainOrmMapper.toUserEntity(user);
        UserEntity savedEntity = repository.save(userEntity);
        return DomainOrmMapper.toUser(savedEntity);
    }

    public Optional<User> findUserById(String userId) {
        return repository.findById(userId)
                .map(DomainOrmMapper::toUser);
    }
}
