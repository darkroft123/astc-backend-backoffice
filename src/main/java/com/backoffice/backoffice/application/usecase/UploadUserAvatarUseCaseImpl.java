package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.UploadUserAvatarUseCase;
import com.backoffice.backoffice.domain.entity.UserEntity;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;
import com.backoffice.backoffice.infrastructure.sender.S3Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.InputStream;
import java.util.UUID;

@ApplicationScoped
public class UploadUserAvatarUseCaseImpl implements UploadUserAvatarUseCase {

    @Inject
    UserRepository userRepository;

    @Inject
    S3Service s3Service;

    @Override
    @Transactional
    public String execute(UUID userId, InputStream file, long size) {

        // 🔹 1. Validar usuario
        UserEntity user = userRepository.findById(userId);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // 🔹 2. Subir archivo (sobrescribe avatar)
        String url = s3Service.uploadUserAvatar(userId, file, size);

        // 🔹 3. Guardar URL en BD
        user.avatarUrl = url;

        userRepository.save(user);

        // 🔹 4. Retornar URL
        return url;
    }
}