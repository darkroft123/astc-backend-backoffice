package com.backoffice.backoffice.infrastructure.controller.graphql;

import com.backoffice.backoffice.infrastructure.client.AuthClientLogger;
import com.backoffice.backoffice.infrastructure.controller.graphql.dto.*;
import com.backoffice.backoffice.infrastructure.security.AuthContext;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.*;

import org.jboss.logging.Logger;

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
        LOG.info("password: " + input.getPassword());
        authContext.assertBackoffice();

        // 🔥 ESTE ES EL PUNTO CLAVE (SALIDA AL MICROSERVICIO)
        LOG.info("➡️ CALLING AUTH-SERVICE createUser");
        LOG.info("➡️ SENDING REQUEST VIA GRAPHQL CLIENT");

        CreateUserResponse response = authClient.createUser(input);

        LOG.info("📤 RESPONSE FROM AUTH-SERVICE:");
        LOG.info(response);

        return response;
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