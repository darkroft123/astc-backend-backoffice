package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.application.in.LoginUseCase;
import com.backoffice.backoffice.domain.entity.UserEntity;
import com.backoffice.backoffice.infrastructure.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.mindrot.jbcrypt.BCrypt;
import io.smallrye.jwt.build.Jwt;

import java.util.Set;

@ApplicationScoped
public class LoginUseCaseImpl implements LoginUseCase {

    @Inject
    UserRepository userRepository;

    @Override
    public String execute(String email, String password) {

        UserEntity user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!BCrypt.checkpw(password, user.passwordHash)) {
            throw new RuntimeException("Invalid password");
        }

        String role = "ADMIN"; // 🔥 TEMPORAL PARA QUE TODO FUNCIONE

        return Jwt.issuer("https://backoffice.example.com")
                .subject(user.id.toString())
                .groups(Set.of(role))
                .claim("email", user.email)
                .sign();
    }
}