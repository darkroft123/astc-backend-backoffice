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
    @RolesAllowed({"ADMIN"})
    public CreateUserResponse createUser(@Name("input") CreateUserInput input) throws Exception {

        LOG.info("🚀 [Backoffice Backend] CREATE USER REQUEST RECEIVED");
        LOG.info("=================================================");
        LOG.info("📥 Incoming createUser Payload:");
        LOG.info("  Username: " + input.getUsername());
        LOG.info("  Email: " + input.getEmail());
        LOG.info("  First Name: " + input.getFirstName());
        LOG.info("  Last Name: " + input.getLastName());
        LOG.info("  Phone: " + input.getPhone());
        LOG.info("  Role ID: " + input.getRoleId());
        LOG.info("  Avatar payload present: " + (input.getAvatarUrl() != null && !input.getAvatarUrl().isBlank()));
        LOG.info("=================================================");

        authContext.assertBackoffice();

        String key = null;
        try {
            if (input.getAvatarUrl() != null && !input.getAvatarUrl().isBlank()) {
                byte[] imageBytes;
                try {
                    imageBytes = Base64.getDecoder().decode(input.getAvatarUrl());
                } catch (IllegalArgumentException decErr) {
                    LOG.error("❌ [Backoffice Backend] Failed to decode base64 avatar image string", decErr);
                    throw decErr;
                }

                String filename = input.getUsername() + ".png";
                
                LOG.info("[MinIO] Upload requested");
                LOG.info("[MinIO] Bucket: " + s3StorageAdapter.getBucket());
                LOG.info("[MinIO] Filename: " + filename);
                LOG.info("[MinIO] Size: " + imageBytes.length + " bytes");
                LOG.info("[MinIO] Content-Type: image/png");

                long startTime = System.currentTimeMillis();
                try {
                    key = s3StorageAdapter.upload(
                            "avatars",
                            filename,
                            new ByteArrayInputStream(imageBytes),
                            imageBytes.length,
                            "image/png"
                    );
                    
                    long duration = System.currentTimeMillis() - startTime;
                    String presignedUrl = s3StorageAdapter.generatePresignedUrl(key, 525600);
                    
                    LOG.info("[MinIO] Upload successful");
                    LOG.info("[MinIO] Object key: " + key);
                    LOG.info("[MinIO] Presigned URL: " + presignedUrl);
                    LOG.info("[MinIO] Upload duration: " + duration + " ms");

                    input.setAvatarUrl(presignedUrl);
                } catch (Exception uploadErr) {
                    LOG.error("[MinIO] Upload failed", uploadErr);
                    LOG.error("[MinIO] Exception message: " + uploadErr.getMessage());
                    throw uploadErr;
                }
            } else {
                LOG.info("[MinIO] Avatar upload skipped because no image was provided");
            }

            LOG.info("➡️ [Backoffice Backend] Calling auth-service createUser API...");
            CreateUserResponse response = authClient.createUser(input);

            LOG.info("📤 [Backoffice Backend] Response received from auth-service:");
            if (response != null) {
                LOG.info("  Created User ID: " + response.getId());
                LOG.info("  Created Username: " + response.getUsername());
                LOG.info("  Created Email: " + response.getEmail());
            } else {
                LOG.info("  Response is null");
            }

            return response;

        } catch (io.smallrye.graphql.client.GraphQLClientException e) {
            LOG.error("💥 [Backoffice Backend] GraphQL client error calling auth-service: " + e.getMessage());
            if (key != null) {
                LOG.warn("[CreateUser] Avatar uploaded successfully but user creation failed");
                LOG.warn("[CreateUser] Uploaded object key: " + key);
                LOG.info("[MinIO] Initiating automatic cleanup for orphaned avatar...");
                s3StorageAdapter.delete(key);
            }
            if (e.getErrors() != null && !e.getErrors().isEmpty()) {
                String errorMsg = e.getErrors().get(0).getMessage();
                LOG.error("💥 [Backoffice Backend] Surfacing friendly error: " + errorMsg);
                throw new org.eclipse.microprofile.graphql.GraphQLException(errorMsg);
            }
            throw e;
        } catch (Exception e) {
            LOG.error("💥 [Backoffice Backend] Exception occurred during createUser flow", e);
            if (key != null) {
                LOG.warn("[CreateUser] Avatar uploaded successfully but user creation failed");
                LOG.warn("[CreateUser] Uploaded object key: " + key);
                LOG.info("[MinIO] Initiating automatic cleanup for orphaned avatar...");
                s3StorageAdapter.delete(key);
            }
            throw e;
        }
    }
    // =========================
    // LIST USERS
    // =========================
    @Query
    @RolesAllowed({"ADMIN", "PROJECT_MANAGER"})
    public List<UserOutput> listUsers() {

        authContext.assertBackofficeOrProjectManager();

        return authClient.listUsers();
    }

    // =========================
    // GET USER
    // =========================
    @Query("getUser")
    @RolesAllowed({"ADMIN", "PROJECT_MANAGER", "TEAM_MEMBER"})
    public UserOutput getUser(@Name("id") String id) {

        authContext.assertBackofficeOrProjectManagerOrTeamMember();

        return authClient.getUser(id);
    }

    // =========================
    // EDIT USER
    // =========================
    @Mutation
    @RolesAllowed({"ADMIN"})
    public UserOutput editUser(
            @Name("id") String id,
            @Name("input") EditUserInput input) throws Exception {

        authContext.assertBackoffice();

        try {
            return authClient.editUser(id, input);
        } catch (io.smallrye.graphql.client.GraphQLClientException e) {
            LOG.error("💥 [Backoffice Backend] GraphQL client error calling auth-service on editUser: " + e.getMessage());
            if (e.getErrors() != null && !e.getErrors().isEmpty()) {
                String errorMsg = e.getErrors().get(0).getMessage();
                LOG.error("💥 [Backoffice Backend] Surfacing friendly error on editUser: " + errorMsg);
                throw new org.eclipse.microprofile.graphql.GraphQLException(errorMsg);
            }
            throw e;
        }
    }

    // =========================
    // DELETE USER
    // =========================
    @Mutation
    @RolesAllowed({"ADMIN"})
    public Boolean deleteUser(@Name("id") String id) {

        authContext.assertBackoffice();

        return authClient.deleteUser(id);
    }
}