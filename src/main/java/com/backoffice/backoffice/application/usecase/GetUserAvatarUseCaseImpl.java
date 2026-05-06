package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.GetUserAvatarUseCase;  // ← IMPORTANTE
import com.backoffice.backoffice.infrastructure.sender.StorageAdapter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class GetUserAvatarUseCaseImpl implements GetUserAvatarUseCase {

    @Inject
    StorageAdapter storageAdapter;

    @Override
    public String execute(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId es obligatorio");
        }

        String objectName = "user/" + userId + "/photo/avatar.jpg";
        
        return storageAdapter.getAvatarPresignedUrl(objectName);
    }
}