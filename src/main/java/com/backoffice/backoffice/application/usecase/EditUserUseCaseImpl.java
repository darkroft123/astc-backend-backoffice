package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.EditUserUseCase;
import com.backoffice.backoffice.domain.model.User;
import com.backoffice.backoffice.domain.entity.UserEntity;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
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

        if (user.getUsername() != null)
            entity.setUsername(user.getUsername());

        if (user.getFirstName() != null)
            entity.setFirstName(user.getFirstName());

        if (user.getLastName() != null)
            entity.setLastName(user.getLastName());

        if (user.getEmail() != null)
            entity.setEmail(user.getEmail());

        if (user.getPhone() != null)
            entity.setPhone(user.getPhone());

        if (user.getAvatarUrl() != null)
            entity.setAvatarUrl(user.getAvatarUrl());

        if (user.getRoleId() != null)
            entity.setRoleId(user.getRoleId());

        entity.setUpdatedAt(Instant.now().atZone(ZoneOffset.UTC).toLocalDateTime());

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