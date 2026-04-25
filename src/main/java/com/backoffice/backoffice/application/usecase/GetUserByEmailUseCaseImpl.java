package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.GetUserByEmailUseCase;
import com.backoffice.backoffice.domain.model.User;
import com.backoffice.backoffice.domain.entity.UserEntity;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;
import com.backoffice.backoffice.application.usecase.UserMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetUserByEmailUseCaseImpl implements GetUserByEmailUseCase {

    @Inject
    UserRepository userRepository;

    @Override
    public User execute(String email) {

        return userRepository.findByEmail(email)
                .map(UserMapper::toDomain)
                .orElse(null);
    }
}