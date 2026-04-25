package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.UploadUserAvatarUseCase;
import com.backoffice.backoffice.domain.entity.UserEntity;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;
import com.backoffice.backoffice.infrastructure.storage.MinioStorageAdapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class UploadUserAvatarUseCaseImpl implements UploadUserAvatarUseCase {

    @Inject
    MinioStorageAdapter storage;

    @Inject
    UserRepository userRepository;

    @Override
    @Transactional
    public String execute(UUID userId, InputStream file, long size) {

        UserEntity user = userRepository.findById(userId);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        String fileId = UUID.randomUUID().toString();

        String path = "document/private/user/" 
                + userId 
                + "/photo/" 
                + fileId;

        String url = storage.upload(path, file, size);

        user.setAvatarUrl(url);
        user.setUpdatedAt(LocalDateTime.now());

        return url;
    }
}