package com.backoffice.backoffice.infrastructure.controller.graphql;

import com.backoffice.backoffice.infrastructure.sender.S3Service;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;
import com.backoffice.backoffice.domain.entity.UserEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.GraphQLApi;

import java.io.InputStream;
import java.util.UUID;

@GraphQLApi
@ApplicationScoped
public class UserFileResource {

    @Inject
    S3Service s3Service;

    @Inject
    UserRepository userRepository;

    @Mutation
    @Transactional
    public String updateUserAvatar(String userId) {

        try {
            UUID uuid = UUID.fromString(userId);

            // 🔹 1. Buscar usuario
            UserEntity user = userRepository.findById(uuid);

            if (user == null) {
                throw new RuntimeException("User not found");
            }

            // 🔹 2. Simulación archivo
            InputStream file = getClass().getClassLoader()
                    .getResourceAsStream("test.jpg");

            if (file == null) {
                throw new RuntimeException("Archivo test.jpg no encontrado");
            }

            long size = file.available();

            // 🔹 3. Subir a MinIO (sobrescribe)
            String url = s3Service.uploadUserAvatar(uuid, file, size);

            // 🔥 4. Guardar en BD
            user.avatarUrl = url;

            // opcional
            user.updatedAt = java.time.LocalDateTime.now();

            userRepository.save(user);

            return url;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating avatar: " + e.getMessage());
        }
    }
}