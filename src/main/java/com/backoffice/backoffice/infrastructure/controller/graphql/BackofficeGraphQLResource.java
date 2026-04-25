package com.backoffice.backoffice.infrastructure.controller.graphql;

import com.backoffice.backoffice.application.in.*;
import com.backoffice.backoffice.domain.model.User;
import com.backoffice.backoffice.infrastructure.controller.graphql.dto.*;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.*;

import java.util.List;
import java.util.UUID;

@GraphQLApi
@ApplicationScoped
public class BackofficeGraphQLResource {

    @Inject CreateUserUseCase createUserUseCase;
    @Inject ListUsersUseCase listUsersUseCase;
    @Inject GetUserDetailUseCase getUserDetailUseCase;
    @Inject EditUserUseCase editUserUseCase;
    @Inject DeleteUserUseCase deleteUserUseCase;

    // =========================
    // CREATE USER
    // =========================
    @Mutation
    public UserOutput createUser(CreateUserInput input) {

        validateCreateUser(input);

        User user = mapToDomain(input);

        UUID id = createUserUseCase.execute(user);

        user.setId(id);

        return UserOutput.fromDomain(user);
    }

    // =========================
    // LIST USERS
    // =========================
    @Query("ListUsers")
    // @RolesAllowed({"admin"})
    public List<UserOutput> listUsers() {

        return listUsersUseCase.execute()
                .stream()
                .map(UserOutput::fromDomain)
                .toList();
    }

    // =========================
    // GET USER DETAIL
    // =========================
    @Query("GetUser")
    @RolesAllowed({"admin"})
    public UserOutput getUser(@Name("id") String id) {

        User user = getUserDetailUseCase.execute(UUID.fromString(id));

        return UserOutput.fromDomain(user);
    }

    // =========================
    // EDIT USER
    // =========================
    @Mutation
    @RolesAllowed({"admin"})
    public UserOutput editUser(
            @Name("id") String id,
            @Name("input") EditUserInput input) {

        User user = mapToDomain(input);

        User updated = editUserUseCase.execute(UUID.fromString(id), user);

        return UserOutput.fromDomain(updated);
    }

    // =========================
    // DELETE USER
    // =========================
    @Mutation
    @RolesAllowed({"admin"})
    public boolean deleteUser(@Name("id") String id) {

        return deleteUserUseCase.execute(UUID.fromString(id));
    }

    // =========================
    // MAPPERS
    // =========================
    private User mapToDomain(CreateUserInput input) {

        User user = new User();

        user.setUsername(input.getUsername());
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setEmail(input.getEmail());
        user.setPhone(input.getPhone());
        user.setPassword(input.getPassword());

        if (input.getRoleId() != null) {
            user.setRoleId(UUID.fromString(input.getRoleId()));
        }

        return user;
    }

    private User mapToDomain(EditUserInput input) {

        User user = new User();

        user.setUsername(input.getUsername());
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setEmail(input.getEmail());
        user.setPhone(input.getPhone());

        if (input.getRoleId() != null) {
            user.setRoleId(UUID.fromString(input.getRoleId()));
        }

        return user;
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