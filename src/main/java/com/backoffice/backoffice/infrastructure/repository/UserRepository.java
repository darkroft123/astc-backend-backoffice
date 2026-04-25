package com.backoffice.backoffice.infrastructure.repository;

import com.backoffice.backoffice.domain.entity.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserEntity, UUID> {

    public List<UserEntity> findUsers(String search, int offset, int size) {

        if (search == null || search.isBlank()) {
            return findAll()
                    .page(offset / size, size)
                    .list();
        }

        return find("LOWER(username) LIKE ?1 OR LOWER(email) LIKE ?1",
                "%" + search.toLowerCase() + "%")
                .page(offset / size, size)
                .list();
    }

    public long countUsers(String search) {

        if (search == null || search.isBlank()) {
            return count();
        }

        return count("LOWER(username) LIKE ?1 OR LOWER(email) LIKE ?1",
                "%" + search.toLowerCase() + "%");
    }

    public Optional<UserEntity> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

}