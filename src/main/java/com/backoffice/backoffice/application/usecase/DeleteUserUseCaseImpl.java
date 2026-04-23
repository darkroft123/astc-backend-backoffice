package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.DeleteUserUseCase;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class DeleteUserUseCaseImpl implements DeleteUserUseCase {

    @Inject
    UserRepository userRepository;

    @Override
    public boolean execute(UUID id) {
        return userRepository.delete(id);
    }
}