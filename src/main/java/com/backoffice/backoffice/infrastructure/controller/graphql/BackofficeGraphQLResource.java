package com.backoffice.backoffice.infrastructure.controller.graphql;

import com.backoffice.backoffice.application.in.*;
import com.backoffice.backoffice.domain.model.User;
import com.backoffice.backoffice.infrastructure.controller.graphql.dto.*;
import com.backoffice.backoffice.infrastructure.repository.RoleRepository;
import com.backoffice.backoffice.domain.entity.RoleEntity;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.*;

import org.jboss.logging.Logger;
import io.quarkus.security.identity.SecurityIdentity;

import java.util.List;
import java.util.UUID;

@GraphQLApi
@ApplicationScoped
public class BackofficeGraphQLResource {

    private static final Logger LOG = Logger.getLogger(BackofficeGraphQLResource.class);

    @Inject CreateUserUseCase createUserUseCase;
    @Inject ListUsersUseCase listUsersUseCase;
    @Inject GetUserDetailUseCase getUserDetailUseCase;
    @Inject EditUserUseCase editUserUseCase;
    @Inject DeleteUserUseCase deleteUserUseCase;

    @Inject RoleRepository roleRepository;

    @Inject
    SecurityIdentity identity;

    // =========================
    // CREATE USER
    // =========================
    @Mutation
    @RolesAllowed({"ADMIN"})
    public UserOutput createUser(CreateUserInput input) {

        LOG.info("🔥 ENTER createUser()");
        LOG.info("👤 USER: " + identity.getPrincipal().getName());
        LOG.info("🔐 ROLES: " + identity.getRoles());

        validateCreateUser(input);

        User user = mapCreate(input);

        UUID id = createUserUseCase.execute(user);

        user.setId(id);

        LOG.info("✅ USER CREATED: " + id);

        return UserOutput.fromDomain(user);
    }

    // =========================
    // LIST USERS
    // =========================
    @Query("ListUsers")
    @RolesAllowed({"ADMIN"})
    public List<UserOutput> listUsers() {

        LOG.info("🔥 ENTER listUsers()");
        LOG.info("👤 USER: " + identity.getPrincipal().getName());
        LOG.info("🔐 ROLES: " + identity.getRoles());
        LOG.info("🛡 ADMIN CHECK REQUIRED");

        return listUsersUseCase.execute()
                .stream()
                .map(UserOutput::fromDomain)
                .toList();
    }

    // =========================
    // GET USER
    // =========================
    @Query("GetUser")
    @RolesAllowed({"ADMIN"})
    public UserOutput getUser(@Name("id") String id) {

        LOG.info("🔥 GET USER id: " + id);

        User user = getUserDetailUseCase.execute(UUID.fromString(id));

        return UserOutput.fromDomain(user);
    }

    // =========================
    // EDIT USER
    // =========================
    @Mutation
    @RolesAllowed({"ADMIN"})
    public UserOutput editUser(
            @Name("id") String id,
            @Name("input") EditUserInput input) {

        LOG.info("🔥 EDIT USER id: " + id);

        User user = mapEdit(input);

        User updated = editUserUseCase.execute(UUID.fromString(id), user);

        LOG.info("✅ USER UPDATED: " + id);

        return UserOutput.fromDomain(updated);
    }

    // =========================
    // DELETE USER
    // =========================
    @Mutation
    @RolesAllowed({"ADMIN"})
    public boolean deleteUser(@Name("id") String id) {

        LOG.info("🔥 DELETE USER id: " + id);

        boolean result = deleteUserUseCase.execute(UUID.fromString(id));

        LOG.info("🗑 DELETE RESULT: " + result);

        return result;
    }

    // =========================
    // MAPPER CREATE
    // =========================
    private User mapCreate(CreateUserInput input) {

        User user = new User();

        user.setUsername(input.getUsername());
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setEmail(input.getEmail());
        user.setPhone(input.getPhone());
        user.setPassword(input.getPassword());

        if (input.getRoleId() != null) {
            user.setRoleId(resolveRole(input.getRoleId()));
        }

        return user;
    }

    // =========================
    // MAPPER EDIT
    // =========================
    private User mapEdit(EditUserInput input) {

        User user = new User();

        user.setUsername(input.getUsername());
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setEmail(input.getEmail());
        user.setPhone(input.getPhone());

        if (input.getRoleId() != null) {
            user.setRoleId(resolveRole(input.getRoleId()));
        }

        return user;
    }

    // =========================
    // ROLE RESOLVER
    // =========================
    private UUID resolveRole(String roleInput) {

        RoleEntity role = roleRepository.findByIdOrCode(roleInput)
                .orElseThrow(() ->
                        new IllegalArgumentException("Rol inválido: " + roleInput)
                );

        return role.getId();
    }

    // =========================
    // VALIDATION
    // =========================
    private void validateCreateUser(CreateUserInput input) {

        if (input == null) {
            throw new IllegalArgumentException("Input requerido");
        }

        if (input.getEmail() == null || input.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email requerido");
        }

        if (input.getUsername() == null || input.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username requerido");
        }

        if (input.getRoleId() == null) {
            throw new IllegalArgumentException("Role requerido");
        }
    }
}