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
import io.smallrye.common.annotation.Blocking;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import io.vertx.core.json.JsonObject;

@GraphQLApi
@ApplicationScoped
@Blocking
public class BackofficeGraphQLResource {

    private static final Logger LOG = Logger.getLogger(BackofficeGraphQLResource.class);

    @Inject
    AuthContext authContext;

    @Inject
    AuthClientLogger authClient;

    @Inject
    S3StorageAdapter s3StorageAdapter;

    @Inject
    @Channel("user-events")
    Emitter<String> userEventsEmitter;

    @Inject
    @Channel("notification-events")
    Emitter<String> notificationEmitter;

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

            if (response != null && response.getId() != null) {
                JsonObject event = new JsonObject();
                event.put("eventType", "USER_CREATED");
                event.put("userId", response.getId());
                userEventsEmitter.send(event.encode());
                LOG.infof("📢 [RabbitMQ] Broadcasted USER_CREATED event for: %s", response.getId());
                notifyUserCreated(response, input);
            }

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
    @RolesAllowed({"ADMIN", "PROJECT_MANAGER", "TEAM_MEMBER"})
    public List<UserOutput> listUsers() {

        authContext.assertBackofficeOrProjectManagerOrTeamMember();

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
            UserOutput response = authClient.editUser(id, input);
            if (response != null && response.getId() != null) {
                JsonObject event = new JsonObject();
                event.put("eventType", "USER_UPDATED");
                event.put("userId", response.getId().toString());
                userEventsEmitter.send(event.encode());
                LOG.infof("📢 [RabbitMQ] Broadcasted USER_UPDATED event for: %s", response.getId());
            }
            return response;
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
 
        Boolean response = authClient.deleteUser(id);
        if (response != null && response) {
            JsonObject event = new JsonObject();
            event.put("eventType", "USER_DELETED");
            event.put("userId", id);
            userEventsEmitter.send(event.encode());
            LOG.infof("📢 [RabbitMQ] Broadcasted USER_DELETED event for: %s", id);
        }
        return response;
    }

    // =========================
    // NOTIFICACIONES AL CREAR USUARIO
    // =========================

    private void notifyUserCreated(CreateUserResponse response, CreateUserInput input) {
        try {
            List<UserOutput> allUsers = authClient.listUsers();

            UserOutput newUser = null;
            UserOutput creador = null;
            java.util.UUID creatorId = authContext.getUserId();
            for (UserOutput u : allUsers) {
                if (u.getId() == null) continue;
                if (response.getId() != null && u.getId().toString().equals(response.getId())) {
                    newUser = u;
                }
                if (creatorId != null && u.getId().equals(creatorId)) {
                    creador = u;
                }
            }

            String fullName = (newUser != null && newUser.getFullName() != null && !newUser.getFullName().isBlank())
                    ? newUser.getFullName()
                    : ((input.getFirstName() + " " + input.getLastName()).trim());
            String roleDisplay = (newUser != null && newUser.getRoleName() != null) ? newUser.getRoleName()
                    : (newUser != null && newUser.getRoleCode() != null ? newUser.getRoleCode() : "usuario");
            String password = (input.getPassword() != null && !input.getPassword().isBlank())
                    ? input.getPassword() : "(oculta)";

            // 1. Credenciales al nuevo usuario
            JsonObject varsUser = new JsonObject()
                    .put("USERNAME", response.getUsername())
                    .put("EMAIL", response.getEmail())
                    .put("PASSWORD", password)
                    .put("ROLE_NAME", roleDisplay);
            sendEmailNotification("ASB000101", response.getId(), response.getEmail(), fullName, varsUser);

            // 2. Aviso SOLO al admin que creo el usuario (nada de difusion masiva)
            int avisos = 0;
            if (creador != null && creador.getEmail() != null && creador.getId() != null
                    && !creador.getEmail().equalsIgnoreCase(response.getEmail())) {
                JsonObject varsAdmin = new JsonObject()
                        .put("NEW_USER_NAME", fullName)
                        .put("NEW_USER_EMAIL", response.getEmail())
                        .put("NEW_USER_ROLE", roleDisplay)
                        .put("CREATED_BY", authContext.getEmail());
                sendEmailNotification("ASB000102", creador.getId().toString(), creador.getEmail(),
                        creador.getFullName(), varsAdmin);
                avisos = 1;
            }

            LOG.infof("📧 [Notifications] USER_CREATED notifications queued: 1 credentials email to %s + %d notice to creator",
                    response.getEmail(), avisos);
        } catch (Exception e) {
            LOG.error("❌ [Notifications] Failed to queue USER_CREATED notifications (user creation is NOT affected)", e);
        }
    }

    private void sendEmailNotification(String code, String userId, String toEmail, String toName, JsonObject variables) {
        try {
            JsonObject message = new JsonObject()
                    .put("notificationCode", code)
                    .put("type", "USER_CREATED")
                    .put("userId", userId)
                    .put("toEmail", toEmail)
                    .put("toName", toName == null ? "" : toName)
                    .put("variables", variables);
            notificationEmitter.send(message.encode());
            LOG.infof("📧 [Notifications] Queued %s -> %s", code, toEmail);
        } catch (Exception e) {
            LOG.errorf(e, "❌ [Notifications] Failed to queue %s -> %s", code, toEmail);
        }
    }
}