package com.backoffice.backoffice.infrastructure.controller.graphql;

import com.backoffice.backoffice.application.in.*;
import com.backoffice.backoffice.domain.model.User;
import com.backoffice.backoffice.infrastructure.controller.graphql.dto.*;
import com.backoffice.backoffice.infrastructure.sender.S3Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.annotation.security.RolesAllowed;

import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.io.InputStream;
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
    @Inject LoginUseCase loginUseCase;

    @Inject S3Service s3Service;
    @Inject JsonWebToken jwt;

    // =========================
    // CREATE USER (PUBLIC)
    // =========================
    @Mutation
    public UserOutput createUser(CreateUserInput input) {

        validateCreateUser(input);

        User user = new User();
        user.username = input.username;
        user.firstName = input.firstName;
        user.lastName = input.lastName;
        user.email = input.email;
        user.password = input.password;
        user.phone = input.phone;
        user.roleId = UUID.fromString(input.roleId);

        return UserOutput.from(createUserUseCase.execute(user));
    }

    // =========================
    // LOGIN (PUBLIC)
    // =========================
    @Mutation
    public LoginOutput login(LoginInput input) {
        String token = loginUseCase.execute(input.email, input.password);
        return new LoginOutput(token);
    }

    // =========================
    // USERS (ADMIN)
    // =========================
    @Query
    @RolesAllowed("ADMIN")
    public List<UserOutput> listUsers() {
        return listUsersUseCase.execute()
                .stream()
                .map(UserOutput::from)
                .toList();
    }

    // =========================
    // GET USER (ADMIN)
    // =========================
    @Query
    @RolesAllowed("ADMIN")
    public UserOutput getUser(@Name("id") String id) {
        return UserOutput.from(
                getUserDetailUseCase.execute(UUID.fromString(id))
        );
    }

    // =========================
    // EDIT USER (ADMIN)
    // =========================
    @Mutation
    @RolesAllowed("ADMIN")
    public UserOutput editUser(
            @Name("id") String id,
            @Name("input") EditUserInput input) {

        User user = new User();
        user.username = input.username;
        user.firstName = input.firstName;
        user.lastName = input.lastName;
        user.email = input.email;
        user.phone = input.phone;

        user.roleId = input.roleId != null
                ? UUID.fromString(input.roleId)
                : null;

        return UserOutput.from(
                editUserUseCase.execute(UUID.fromString(id), user)
        );
    }

    // =========================
    // DELETE USER (ADMIN)
    // =========================
    @Mutation
    @RolesAllowed("ADMIN")
    public boolean deleteUser(@Name("id") String id) {
        return deleteUserUseCase.execute(UUID.fromString(id));
    }

    private void validateCreateUser(CreateUserInput input) {

        if (input == null)
            throw new IllegalArgumentException("Input requerido");

        if (input.email == null || input.email.isBlank())
            throw new IllegalArgumentException("Email requerido");

        if (input.password == null || input.password.isBlank())
            throw new IllegalArgumentException("Password requerido");

        if (input.roleId == null)
            throw new IllegalArgumentException("Role requerido");
    }
}