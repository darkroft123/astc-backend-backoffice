package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.UploadUserAvatarUseCase;
import com.backoffice.backoffice.infrastructure.sender.StorageAdapter;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.InputStream;
import java.util.UUID;

@ApplicationScoped
public class UploadUserAvatarUseCaseImpl implements UploadUserAvatarUseCase {

    private final StorageAdapter storageAdapter;

    @Inject
    public UploadUserAvatarUseCaseImpl(StorageAdapter storageAdapter) {
        this.storageAdapter = storageAdapter;
    }

    @Override
    @Transactional
    public String execute(UUID userId, InputStream file, Long fileSize, String originalFileName, String contentType) {

        // ✅ Validación básica
        if (file == null) {
            throw new IllegalArgumentException("El archivo es obligatorio");
        }

        // ✅ Validación por extensión (LA CLAVE)
        String extension = getFileExtension(originalFileName);

        if (!extension.matches("\\.(jpg|jpeg|png)$")) {
            throw new IllegalArgumentException("Solo se permiten imágenes (.jpg, .jpeg, .png)");
        }

        // 🔥 Nombre dinámico (seguro)
        String objectName = "user/" + userId + "/photo/avatar.jpg";

        // 🚀 Subida (sin depender de fileSize)
        storageAdapter.uploadAvatar(objectName, file, fileSize != null ? fileSize : 0, contentType);

        return objectName;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".jpg"; // default
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    private String resolveContentType(String extension) {
        return switch (extension) {
            case ".png" -> "image/png";
            case ".jpeg", ".jpg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }
}