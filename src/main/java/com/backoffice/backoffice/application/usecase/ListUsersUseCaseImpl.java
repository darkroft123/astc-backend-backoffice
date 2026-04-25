package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.ListUsersUseCase;
import com.backoffice.backoffice.domain.model.User;
import com.backoffice.backoffice.domain.entity.UserEntity;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;
import com.backoffice.backoffice.application.usecase.UserMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ListUsersUseCaseImpl implements ListUsersUseCase {

    @Inject
    UserRepository userRepository;

    @Override
    public List<User> execute() {

        return userRepository.listAll()
                .stream()
                .map(UserMapper::toDomain)
                .toList();
    }
}