package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.CreateUserUseCase;
import com.backoffice.backoffice.domain.entity.UserEntity;
import com.backoffice.backoffice.domain.model.User;
import com.backoffice.backoffice.domain.model.UserStatus;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    @Inject
    UserRepository userRepository;

    @Override
@Transactional
public User execute(User user) {

    if (user == null)
        throw new IllegalArgumentException("User cannot be null");

    if (isBlank(user.email))
        throw new IllegalArgumentException("Email is required");

    if (isBlank(user.username))
        throw new IllegalArgumentException("Username is required");

    if (isBlank(user.password))
        throw new IllegalArgumentException("Password is required");

    if (user.roleId == null)
        throw new IllegalArgumentException("Role is required");

    if (user.id == null)
        user.id = UUID.randomUUID();

    if (user.status == null)
        user.status = UserStatus.ACTIVE;

    // 🔥 HASH PASSWORD AQUÍ
    String hashed = BCrypt.hashpw(user.password, BCrypt.gensalt());

    UserEntity entity = UserMapper.toEntity(user);

    entity.passwordHash = hashed;

    entity.fullName = buildFullName(user.firstName, user.lastName);

    entity.createdAt = LocalDateTime.now();
    entity.updatedAt = LocalDateTime.now();

    userRepository.save(entity);

    return UserMapper.toDomain(entity);
}

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String buildFullName(String firstName, String lastName) {
        return (firstName != null ? firstName : "") +
               " " +
               (lastName != null ? lastName : "");
    }
}