package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.EditUserUseCase;
import com.backoffice.backoffice.domain.entity.UserEntity;
import com.backoffice.backoffice.domain.model.User;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;
import com.backoffice.backoffice.application.usecase.UserMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.UUID;

@ApplicationScoped
public class EditUserUseCaseImpl implements EditUserUseCase {

    @Inject
    UserRepository userRepository;

    @Override
    @Transactional
    public User execute(UUID id, User user) {

        UserEntity entity = userRepository.findById(id);

        if (entity == null) {
            throw new RuntimeException("User not found");
        }

        if (user.username != null) entity.username = user.username;
        if (user.email != null) entity.email = user.email;
        if (user.firstName != null) entity.firstName = user.firstName;
        if (user.lastName != null) entity.lastName = user.lastName;
        if (user.phone != null) entity.phone = user.phone;
        if (user.avatarUrl != null) entity.avatarUrl = user.avatarUrl;

        if (user.status != null) {
            entity.status = user.status.name();
        }

        userRepository.save(entity);

        return UserMapper.toDomain(entity);
    }
}