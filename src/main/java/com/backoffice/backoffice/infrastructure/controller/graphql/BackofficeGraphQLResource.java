package com.backoffice.backoffice.infrastructure.controller.graphql;

import com.backoffice.backoffice.infrastructure.client.AuthClientLogger;
import com.backoffice.backoffice.infrastructure.controller.graphql.dto.*;
import com.backoffice.backoffice.infrastructure.security.AuthContext;
import com.backoffice.backoffice.infrastructure.storage.S3StorageAdapter;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.*;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
@GraphQLApi
@ApplicationScoped
@RolesAllowed({"ADMIN"})
public class BackofficeGraphQLResource {

    private static final Logger LOG = Logger.getLogger(BackofficeGraphQLResource.class);

    @Inject
    AuthContext authContext;

    @Inject
    AuthClientLogger authClient;

    @Inject
    S3StorageAdapter s3StorageAdapter;

    // =========================
    // CREATE USER
    // =========================
  
    @Mutation
public CreateUserResponse createUser(@Name("input") CreateUserInput input) {

    LOG.info("🚀 CREATE USER START");
    LOG.info("=================================");
    LOG.info("📥 INPUT BACKOFFICE:");
    LOG.info("username: " + input.getUsername());
    LOG.info("email: " + input.getEmail());
    LOG.info("roleId: " + input.getRoleId());

    authContext.assertBackoffice();

    try {

        if (input.getAvatarUrl() != null &&
            !input.getAvatarUrl().isBlank()) {

            LOG.info("📸 Avatar recibido en base64");

            byte[] imageBytes =
                    Base64.getDecoder().decode(input.getAvatarUrl());

            String key = s3StorageAdapter.upload(
                    "avatars",
                    input.getUsername() + ".png",
                    new ByteArrayInputStream(imageBytes),
                    imageBytes.length,
                    "image/png"
            );

            String publicUrl =
                    s3StorageAdapter.getPublicUrl(key);

            LOG.info("✅ Avatar subido a MinIO:");
            LOG.info(publicUrl);

            input.setAvatarUrl(publicUrl);
        }

        LOG.info("➡️ CALLING AUTH-SERVICE createUser");

        CreateUserResponse response =
                authClient.createUser(input);

        LOG.info("📤 RESPONSE FROM AUTH-SERVICE:");
        LOG.info(response);

        return response;

    } catch (Exception e) {

        LOG.error("💥 Error creando usuario", e);
        throw e;
    }
}
    // =========================
    // LIST USERS
    // =========================
    @Query
    public List<UserOutput> listUsers() {

        authContext.assertBackoffice();

        return authClient.listUsers();
    }

    // =========================
    // GET USER
    // =========================
    @Query
    public UserOutput getUser(@Name("id") String id) {

        authContext.assertBackoffice();

        return authClient.getUser(id);
    }

    // =========================
    // EDIT USER
    // =========================
    @Mutation
    public UserOutput editUser(
            @Name("id") String id,
            @Name("input") EditUserInput input) {

        authContext.assertBackoffice();

        return authClient.editUser(id, input);
    }

    // =========================
    // DELETE USER
    // =========================
    @Mutation
    public Boolean deleteUser(@Name("id") String id) {

        authContext.assertBackoffice();

        return authClient.deleteUser(id);
    }
}