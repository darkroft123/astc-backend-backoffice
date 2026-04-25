package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.GetUserDetailUseCase;
import com.backoffice.backoffice.domain.model.User;
import com.backoffice.backoffice.domain.entity.UserEntity;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;
import com.backoffice.backoffice.application.usecase.UserMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetUserDetailUseCaseImpl implements GetUserDetailUseCase {

    @Inject
    UserRepository userRepository;

    @Override
    public User execute(UUID id) {

        UserEntity entity = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserMapper.toDomain(entity);
    }
}