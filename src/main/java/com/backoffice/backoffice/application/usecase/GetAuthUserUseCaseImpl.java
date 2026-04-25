package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.GetAuthUserUseCase;
import com.backoffice.backoffice.domain.entity.UserEntity;
import com.backoffice.backoffice.domain.model.User;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetAuthUserUseCaseImpl implements GetAuthUserUseCase {

    @Inject
    UserRepository userRepository;

    @Override
    public User execute(String email) {

        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toDomain(entity);
    }

    private User toDomain(UserEntity e) {
        User u = new User();
        u.setId(e.getId());
        u.setUsername(e.getUsername());
        u.setEmail(e.getEmail());
        u.setFirstName(e.getFirstName());
        u.setLastName(e.getLastName());
        u.setFullName(e.getFullName());
        u.setPhone(e.getPhone());
        u.setAvatarUrl(e.getAvatarUrl());
        u.setRoleId(e.getRoleId());
        return u;
    }
}